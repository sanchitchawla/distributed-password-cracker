import org.apache.commons.codec.digest.Crypt

/**
  * Created by sanch on 23-Jan-18.
  */
object PasswordCracker {

  def cracker(startRange: String, endRange: String, hash:String): Boolean ={
    def helper(curr: String, found: Boolean): Boolean = {
      if (getHashed(curr) == hash) true
      else {
        helper(getNext(curr), found)
      }
    }
    if (startRange == endRange) false else helper(startRange, found = false)
  }

  def getHashed(curr: String): String = Crypt.crypt(curr, "ic")
  def getNext(curr: String): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    "Sunny is very cool"
  }
  def main(args: Array[String]): Unit = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    println(chars)
  }

}
