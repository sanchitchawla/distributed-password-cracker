import org.apache.commons.codec.digest.Crypt

import scala.collection.mutable.HashMap
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable.ParArray


/**
  * Created by sanch on 23-Jan-18.
  */

object ParallelCracker {

  var found = ""

  val temp: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList
  val all_chars: HashMap[Char,Char] = new HashMap()
  val char_array: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList

  for(i <- temp.indices){
    all_chars += (temp(i%temp.length)->temp((i+1)%temp.length))
  }

  def toLong(s: String): Long = {
    var rs: Long = 0
    var currentE = 0
    var i = s.length-1
    while (i>=0){
      val c = s.charAt(i)
      val cVal = char_array.indexOf(c) + 1

      rs += cVal * Math.pow(62, currentE).asInstanceOf[Long]
      currentE += 1
      i-=1
    }
    rs
  }

  def findSize(startString: String, endString: String): Long = {
    toLong(endString) - toLong(startString) + 1
  }

  def createList(startString: String, endString: String) = {
    val size = findSize(startString, endString).toInt
    var lst = new ParVector[String]()

    var currentString = startString

    for(i <- 0 to size-1){
      lst = lst:+currentString
      currentString = getNext(currentString)

    }
    lst


  }

  def checkHashed(s: String, hash: String) = {
    println(s)

    if (getHashed(s).equals(hash)){
      found = s
    }
    found!=""

  }

  def cracker(startRange: String, endRange: String, hash:String): Boolean ={

    val passwordList = createList(startRange,endRange)

    println(passwordList.size)

    passwordList.foreach{ e => checkHashed(e,hash)}
    passwordList.exists(checkHashed(_,hash))
    Vector(1,2,3,5,2,62,6,16,1).grouped(4)

    // foreach spawn thread for each element
    // Grouped to chunk then parallel for for each chunk, each chunk do sequentially

//    passwordList.foreach{ e => println(e)}

    found!=""

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

  def main(args: Array[String]): Unit = {

    val hash = "ic/VuctZ70TSU"
    print(cracker("A","AA",hash))
  }

}
