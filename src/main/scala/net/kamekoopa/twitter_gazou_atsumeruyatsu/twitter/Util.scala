package net.kamekoopa.twitter_gazou_atsumeruyatsu.twitter

import twitter4j.{Paging, Status}
import java.io.File
import java.net.{URLEncoder, URL}
import scalax.io.Resource


/**
 * 未分類ユーティリティ
 */
private[twitter] object Util {

  /**
   * Paginオブジェクトを返す。ステータスがNoneなら空のPagingオブジェクトを返す
   *
   * @param maybeStatus ステータスかもしれない値
   * @return Paingオブジェクト
   */
  def paging(maybeStatus: Option[Status]): Paging = {
    maybeStatus.fold(new Paging){ status =>
      val paging = new Paging(status.getId)
      paging.setCount(20)
      paging
    }
  }

  /**
   * ツイートに含まれている画像URLを返す。対応してないサービスは無視
   *
   * @param status ツイート(ステータス)
   *
   * @return 画像URLのリスト。対応してないサービスは無視
   */
  def getImageUrls(status: Status) = {

    val mediaEntities = status.getMediaEntities.map(_.getMediaURL).toList

    val urlEntities = status.getURLEntities.map { entity =>

      val url = entity.getExpandedURL
      if(url.startsWith("http://twitpic.com")){
        Some(url.replace("twitpic.com", "twitpic.com/show/full"))
      }else{
        None
      }
    }.collect{case Some(url) => url}.toList //Noneのやつ(知らないURL)を捨てる

    mediaEntities ++ urlEntities
  }

  /**
   * urlからリソースをダウンロードする
   *
   * @param urlString ダウンロード元
   * @param outputDir ダウンロード先
   *
   * @return ダウンロードされたファイルを表すFileか、失敗したらThrowable
   */
  def download(urlString: String, outputDir: File): Either[Throwable, File] = {
    try {

      val url = new URL(urlString)

      print(s"$url downloading ... ")
      val img = Resource.fromURL(url).byteArray
      println("done")

      val outputFile = outputDir.getPath + "/" + URLEncoder.encode(url.toString, "utf-8")
      Resource.fromFile(outputFile).write(img)

      Right(new File(outputFile))

    } catch {
      case t: Throwable => Left(t)
    }
  }
}
