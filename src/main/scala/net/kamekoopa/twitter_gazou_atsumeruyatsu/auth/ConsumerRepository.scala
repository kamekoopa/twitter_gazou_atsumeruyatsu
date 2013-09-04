package net.kamekoopa.twitter_gazou_atsumeruyatsu.auth

/**
 * コンシューマキーとかのリポジトリ
 */
trait ConsumerRepository {

  def getConsumerKey: String

  def getConsumerSecret: String
}


object ConsumerRepositoryImpl{

  /**
   * 気持ち程度の難読化版
   */
  implicit object Obfuscation extends ConsumerRepository {

    def getConsumerKey = {
      unobfuscator("\\x25\\x19\\x48\\x06\\x32\\x0b\\x06\\x34\\x13\\x2a\\x48\\x27\\x4e\\x17\\x4a\\x2b\\x12\\x1b\\x3b\\x29\\x09\\x18")
    }

    def getConsumerSecret = {
      unobfuscator("\\x18\\x18\\x36\\x1d\\x4b\\x4a\\x11\\x4d\\x27\\x47\\x18\\x32\\x25\\x48\\x2f\\x1a\\x28\\x12\\x2d\\x2e\\x4b\\x18\\x29\\x39\\x17\\x31\\x12\\x11\\x3e\\x32\\x26\\x16\\x26\\x09\\x4e\\x38\\x4a\\x2c\\x4b\\x2c\\x14\\x2e")
    }

    private def unobfuscator(hex: String): String = {
      val bytes = hex.split("\\\\x").tail.map({ hexString =>
        val i = Integer.parseInt(hexString, 16) ^ 127
        i.toByte
      })
      new String(bytes)
    }
  }
}
