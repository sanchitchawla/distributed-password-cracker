
import scala.collection.mutable

/**
  * Created by sanch on 29-Jan-18.
  */
object ServerMain{
  
  val temp: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList
  val all_chars: mutable.HashMap[Char,Char] = new mutable.HashMap()
  for(i <- temp.indices){
    all_chars += (temp(i%temp.length)->temp((i+1)%temp.length))
  }

  var workerToJob = new mutable.HashMap[String,Job]()

  var jobIdToSize = new mutable.HashMap[Int,Long]()

  val CHUNK_SIZE = 62

  def setJob(worker: String,job: Job): Unit = {
    workerToJob += (worker -> job)
  }

  def storeJobSize(jobId: Int, totalSize: Long): Unit ={
    jobIdToSize += (jobId -> totalSize)
  }

  def jobChunkDone(job: Job): Unit ={
    val jobId = job.getJobId
    jobIdToSize(jobId) = jobIdToSize(jobId)-CHUNK_SIZE

    if(jobIdToSize(jobId) <= 0){
      // The entire Job is completed
    }
  }

  def splitAndQueue(start:String , end: String): Unit ={
    var currentEnd = start
    var currentStart = start
    while (toLong(currentEnd) < toLong(end)){

      currentEnd = nextN(currentEnd,CHUNK_SIZE-1)
      if(toLong(currentEnd)>toLong(end)) currentEnd = end

      println(currentStart,currentEnd)
      // add to queue
      currentStart = nextN(currentEnd,1)
    }

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
      rs = getNext(rs)
      i+=1
    }
    rs
  }

  def findSize(startString: String, endString: String): Long = {
    toLong(endString) - toLong(startString) + 1
  }

  def shutdownWorkers(): Unit = {
    // TODO: Send a post request to all the workers and receive an ack back to shutdown

  }

  // TODO:  Generate range and give startString and endString to every worker

}