package net.kamekoopa.twitter_gazou_atsumeruyatsu.twitter

import twitter4j.{Twitter => Twitter4j, Status}
import java.io.File
import net.kamekoopa.twitter_gazou_atsumeruyatsu.CliArgs
import java.util.concurrent.TimeUnit

/**
 * 見張るやつ
 *
 * @param account 対象アカウント
 * @param twitter twitter4jのインスタンス
 * @param outputDir 画像出力先
 * @param interval リクエストのインターバル(ミリ秒)
 * @param latestStatus 取得したことのある最新のツイート
 * @param stopInstruction 見張り番に対して停止指示が発行されているかどうか
 */
class Lookout private(
  private val account: String,
  private val twitter: Twitter4j,
  private val outputDir: File,
  private val interval: Long,
  private val latestStatus: StatusHolder = StatusHolder.empty,
  private var stopInstruction: Boolean = false
) {

  /**
   * 見張り開始
   */
  def start() = {

    new Runnable {
      def run() {
        watch()
      }
    }.run()

    println("finish")
  }

  /**
   * 見張る処理
   */
  private def watch() = {

    //停止指示が出るまで無限ループ
    while(!stopInstruction){

      import scala.collection.JavaConversions._

      val timeline = twitter.getUserTimeline(account, Util.paging(latestStatus.get))
      val imageDownloadSuccessStatusList = timeline.map({ status =>

        val downloadResults = Util.getImageUrls(status).map { url =>
          Util.download(url, outputDir)
        }

        //ダウンロード失敗したものがあるかどうか
        val hasError = downloadResults.count {
          case Left(t) => true
          case Right(d) => false
        } != 0

        //どれか一つでもダウンロード失敗したらこのステータスの処理は失敗したことにする
        if (hasError){
          None
        }else{
          Some(status)
        }
      }).collect({ case Some(s) => s }) //失敗してないやつだけ集める

      //最新ツイート更新
      latestStatus := imageDownloadSuccessStatusList.headOption

      //停止指示が出てなければインターバル分待つ
      if(!stopInstruction){
        this synchronized {
          println(s"wait $interval (ms)")
          this.wait(interval)
        }
      }
    }

    //停止指示が出てループを脱出した時にしかここには来ない
    //停止処理完了待ちになってる人たちを動かす
    this synchronized {
      notifyAll()
    }
  }

  /**
   * 停止してください通知
   */
  def notifyStop() = {

    stopInstruction = true
    this synchronized {
      notifyAll() //インターバル待ちになってるやつ動かす

      //停止処理終わったよnotifyが来るまで待つ
      println("waiting stop")
      this.wait()
    }
  }
}
object Lookout {

  def apply(twitter: Twitter4j, cliArgs: CliArgs) = {

    val waitMillis = TimeUnit.MINUTES.toMillis(cliArgs.fetchInterval)
    new Lookout(cliArgs.account, twitter, cliArgs.imgDir, waitMillis)
  }
}


/**
 * 最新ステータス管理するためのヘルパークラス
 *
 * @param maybeStatus ステータスかもしれないの
 */
private class StatusHolder(private var maybeStatus: Option[Status]) {
  /**
   * このクラスがステータスを保持しているかどうか
   * @return 保持してればtrue
   */
  def isHold = {
    maybeStatus.isDefined
  }

  /**
   * 保持しているのを返す
   * @return ステータスかもしれない値
   */
  def get = {
    maybeStatus
  }

  /**
   * 内部で保持しているステータスを更新する
   *
   * @param status ステータスかもしれない値。Noneの時は更新しない
   */
  def := (status: Option[Status]) = {
    maybeStatus = status match {
      case Some(s) => status
      case None => println("status was not updated"); maybeStatus
    }
  }
}
private object StatusHolder {
  def empty = new StatusHolder(None)
  def apply(status: Status) = new StatusHolder(Some(status))
}


