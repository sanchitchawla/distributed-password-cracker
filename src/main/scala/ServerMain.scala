
import scala.collection.mutable.HashMap

/**
  * Created by sanch on 29-Jan-18.
  */
object ServerMain{


  val temp: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList
  val all_chars: HashMap[Char,Char] = new HashMap()
  for(i <- temp.indices){
    all_chars += (temp(i%temp.length)->temp((i+1)%temp.length))
  }

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

  def toLong(s: String): Long = {
    var rs: Long = 0
    var currentE = 0
    var i = s.length-1
    while (i>=0){
      val c = s.charAt(i)
      val cVal = temp.indexOf(c) + 1

      rs += cVal * Math.pow(62, currentE).asInstanceOf[Long]
      currentE += 1
      i-=1
    }
    rs
  }

  def nextN(string: String, n: Long): String = {
    var rs = string
    var i = 0L

    while (i<n){
      rs = getNext(rs);
      i+=1
    }
    rs
  }

  def findSize(startString: String, endString: String): Long = {
    toLong(endString) - toLong(startString) + 1
  }
  def main(args: Array[String]): Unit = {

    //  val server = new Server("local")
    //  println(server.receive())

    println(toLong("A"))
    println(toLong("AA"))
    println(findSize("A","AD"))
    println(nextN("A",62))


  }

}
