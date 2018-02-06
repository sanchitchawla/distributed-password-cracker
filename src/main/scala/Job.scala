//class Job(jobId: Int, startString: String, endString: String) {
//
//  def getJobId(): Int ={
//    jobId
//  }
//
//  def getStartString(): String = {
//    startString
//  }
//
//  def getEndString(): String = {
//    endString
//  }
//
//}
/**
  * Created by sanch on 01-Feb-18.
  */

// For the queue
case class Job(jobId: Int, startString: String, endString: String, hash: String) {

  def getJobId: Int ={
    jobId
  }

  def getStartString: String = {
    startString
  }

  def getEndString: String = {
    endString
  }

  def getHash: String ={
    hash
  }

}
