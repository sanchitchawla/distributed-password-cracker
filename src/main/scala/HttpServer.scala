import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable.HashMap
import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpServer {
  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var jobId: AtomicInteger = new AtomicInteger(1)
  var workerToJob = new HashMap[String,Job]()
  var jobs: List[Job] = List()

  final case class dispatchedJob(hash: String)

  // formats for unmarshalling and marshalling
  implicit val jobFormat = jsonFormat4(Job)
  implicit val dispatchedJobFormat = jsonFormat1(dispatchedJob)

  var jobIdToSize = new HashMap[AtomicInteger,Long]()
  var jobIdToIp = new HashMap[AtomicInteger,String]()


  val char_array: List[Char] = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toList
  val all_chars: HashMap[Char,Char] = new HashMap()
  for(i <- char_array.indices){
    all_chars += (char_array(i%char_array.length)->char_array((i+1)%char_array.length))
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


  def nextStr(startString: String, pos: Int): String = {
    def nextChar(c: Char): Char = {
      all_chars(c)
    }

    def flip(s: List[Char]): List[Char] = {
      val n = s.length-pos

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

    flip(startString.toList).mkString

  }



  def findSize(startString: String, endString: String): Long = {
    toLong(endString) - toLong(startString) + 1
  }

  def splitAndQueue(job:Job): Unit ={
    var start = job.getStartString
    var end = job.getEndString
    var jobId = job.getJobId

    var currentEnd = start
    var currentStart = start
    while (toLong(currentEnd) < toLong(end)){
      currentEnd = currentStart
      currentEnd = nextStr(currentEnd,2)

      if(toLong(currentEnd)>toLong(end)) currentEnd = end

      println("chunk: "+currentStart+" -> "+currentEnd)
      currentStart = nextStr(currentEnd,1)



//      add to queue


    }

  }

  // (fake) async database query api
  def fetchItem(jobId: Long): Future[Option[Job]] = Future {
    jobs.find(o => o.getJobId == jobId)
  }



  def saveJob(job: dispatchedJob): Future[Done] = {
    jobs = job match {
            case dispatchedJob(hash) => {
              val currentId = jobId.getAndIncrement()
              val totalSize = findSize("A","99999999")
              jobIdToSize += (jobId -> totalSize)

              val j = Job(currentId, "A", "99999999", hash)

              println("Job Created")
              val thread = new Thread{
                override def run: Unit = {
                  splitAndQueue(j)
                }
              }
              thread.start()
//              splitAndQueue(j)
              j :: jobs

            }
            case _            => jobs

    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("getJob" / LongNumber) { id =>
          // there might be no item for a given id
          val maybeJob: Future[Option[Job]] = fetchItem(id)

          onSuccess(maybeJob) {
            case Some(job) => complete(job)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      } ~
        post {
          path("createJob") {
            entity(as[dispatchedJob]) { job =>

              val saved: Future[Done] = saveJob(job)
              onComplete(saved) { done =>
                complete("Job created")
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8082)
    println(s"Server online at http://0.0.0.0:8082/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
    sys.addShutdownHook({
      println("Shutting down workers ")
      ServerMain.shutdownWorkers()
      println("All workers are safely shut")
    })
  }
}