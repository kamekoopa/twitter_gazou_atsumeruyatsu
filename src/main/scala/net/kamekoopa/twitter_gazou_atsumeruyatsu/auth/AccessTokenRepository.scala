package net.kamekoopa.twitter_gazou_atsumeruyatsu.auth

import scalax.io.{Codec, Resource}
import java.io.File
import twitter4j.auth.AccessToken
import twitter4j.Twitter


/**
 * アクセストークンとかを取得するやつ
 *
 * @param twitter twitter4jのインスタンス
 */
class AccessTokenRepository(twitter: Twitter) {

  /**
   * 永続化されてるアクセストークンがあればそれを取得する。なければ関数で指定された処理を利用して取得する
   *
   * @param atokenFetcher アクセストークンがなかった時に利用する取得処理
   *
   * @return アクセストークン
   */
  def getAccessToken()(atokenFetcher: Twitter => Either[Throwable, AccessToken]): Either[Throwable, AccessToken] = {

    AccessTokenReadWriteHelper.readAccessToken().fold(
      for(
        token <- atokenFetcher(twitter).right;
        wroteToken <- AccessTokenReadWriteHelper.writeAccessToken(token).right
      )yield wroteToken
    )(
      Right(_)
    )
  }

  /**
   * アクセストークン永続化のヘルパー的なやつ
   */
  private object AccessTokenReadWriteHelper {

    private implicit val codec = Codec.UTF8

    private val atoken = Resource.fromFile(new File(".atoken"))
    private val asecret = Resource.fromFile(new File(".asecret"))

    def readAccessToken(): Option[AccessToken] = {

      val token = atoken.string
      val secret = asecret.string

      if(token.isEmpty && secret.isEmpty){
        None
      }else{
        Some(new AccessToken(token, secret))
      }
    }

    def writeAccessToken(accessToken: AccessToken): Either[Throwable, AccessToken] = {
      try {

        atoken.write(accessToken.getToken)
        asecret.write(accessToken.getTokenSecret)

        Right(accessToken)

      } catch {
        case e: Throwable => Left(e)
      }
    }
  }
}
