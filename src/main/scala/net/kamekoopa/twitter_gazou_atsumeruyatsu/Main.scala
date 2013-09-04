package net.kamekoopa.twitter_gazou_atsumeruyatsu

import twitter4j.{Twitter => Twitter4j}
import java.io.File
import net.kamekoopa.twitter_gazou_atsumeruyatsu.twitter.{Lookout, Twitter}

/**
 * エントリポイント
 */
object Main {
  def main(args: Array[String]) {

    val cliArgs = CliArgs(args)

    import net.kamekoopa.twitter_gazou_atsumeruyatsu.auth.ConsumerRepositoryImpl.Obfuscation
    val eitherTwitter = Twitter.getInstance

    eitherTwitter.fold(
      { throwable =>
        println("obtaining twitter-instance failed.")
        throwable.printStackTrace()
        sys.exit(1)
      },
      { twitter =>
        exec(cliArgs)(twitter)
      }
    )
  }

  def exec(cliArgs: CliArgs)(twitter: Twitter4j): Unit = {

    val lookout = Lookout(twitter, cliArgs)

    sys.ShutdownHookThread {
      lookout.notifyStop()
    }

    lookout.start()
  }
}

/**
 * コマンドライン引数を表すクラス
 *
 * @param account アカウント
 * @param imgDir ダウンロード先ディレクトリ
 * @param fetchInterval 取得間隔
 */
case class CliArgs(account: String, imgDir: File, fetchInterval: Long)

/**
 * コマンドライン引数解析してCliArgs作るやつ
 */
object CliArgs {
  def apply(args: Array[String]): CliArgs = {
    import org.clapper.argot._

    val parser = new ArgotParser("kneeso collector")

    val targetAccount = parser.option[String](List("a", "account"), "account", "target account(require '@')"){ (str, opt) =>
      str
    }

    val output = parser.option[File](List("o", "output"), "dir", "image output dir"){ (str, opt) =>
      val file = new File(str)
      if ( !file.isDirectory ){
        parser.usage(s"${file.getPath} does not exists or not directory.")
      }else{
        file
      }
    }

    val fetchInterval = parser.option[Long](List("i", "interval"), "min", "fetch interval(min)"){ (str, opt) =>
      str.toLong
    }

    parser.parse(args)

    val arg = for(
      account <- targetAccount.value;
      dir <- output.value
    ) yield {
      val interval = fetchInterval.value.getOrElse(60L)
      new CliArgs(account, dir, interval)
    }

    arg match {
      case Some(a) => a
      case None => {
        parser.usage()
        sys.exit(1)
      }
    }
  }
}

