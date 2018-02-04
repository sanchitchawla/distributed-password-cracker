/**
  * Created by sanch on 01-Feb-18.
  */

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

}