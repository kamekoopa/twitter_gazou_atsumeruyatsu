package net.kamekoopa.twitter_gazou_atsumeruyatsu.twitter

import net.kamekoopa.twitter_gazou_atsumeruyatsu.auth.{AccessTokenRepository, ConsumerRepository}
import twitter4j.{Twitter => Twitter4j, TwitterFactory}

/**
 */
object Twitter {

  def getInstance(implicit consRepo: ConsumerRepository): Either[Throwable, Twitter4j] = {

    val twitter = new TwitterFactory().getInstance()
    twitter.setOAuthConsumer(consRepo.getConsumerKey, consRepo.getConsumerSecret)

    val eitherAccessToken = new AccessTokenRepository(twitter).getAccessToken(){ twitter =>

      val reqToken = twitter.getOAuthRequestToken

      println(s"""
        | Access the following url and get the PIN.
        | Then enter the PIN you got.
        | ${reqToken.getAuthorizationURL}
        """.stripMargin
      )
      val pin = readLine("PIN > ")

      try {

        val accessToken = twitter.getOAuthAccessToken(reqToken, pin)
        Right(accessToken)

      } catch {
        case e: Throwable => Left(e)
      }
    }

    eitherAccessToken.right.map { accessToken =>
      twitter.setOAuthAccessToken(accessToken)
      twitter
    }
  }
}
