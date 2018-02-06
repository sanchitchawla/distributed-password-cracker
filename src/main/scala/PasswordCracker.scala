import org.apache.commons.codec.digest.Crypt

import scala.collection.mutable.HashMap

/**
  * Created by sanch on 23-Jan-18.
  */

// TODO : Make this threaded
object PasswordCracker {

  val temp: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList
  val all_chars: HashMap[Char,Char] = new HashMap()
  for(i <- temp.indices){
    all_chars += (temp(i%temp.length)->temp((i+1)%temp.length))
  }

  def cracker(startRange: String, endRange: String, hash:String): Boolean ={
    def helper(curr: String, found: Boolean): Boolean = {
      if (getHashed(curr).equals(hash)) true
      else if (curr.equals(endRange)) false
      else {
        helper(getNext(curr), found)
      }
    }
    helper(startRange, found = false)
  }

  def getHashed(curr: String): String = Crypt.crypt(curr, "ic")

  def nextChar(c: Char): Char = {
    all_chars(c)
  }

  def flip(s: List[Char]): List[Char] = {
    val n = s.length-1

    def helper(s: List[Char], i: Int): List[Char] = {
      if(i < 0) {
        'A' :: s
      }
      else{
        val next_c = nextChar(s(i))
        var new_s = s.updated(i, next_c)
        if(new_s(i) == 'A') new_s = helper(new_s,i-1)

        new_s
      }
    }
    helper(s,n)
  }

  def getNext(inputString: String): String = {
    flip(inputString.toList).mkString
  }

}
