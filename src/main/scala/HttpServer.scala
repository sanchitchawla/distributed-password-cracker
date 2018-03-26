import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import java.util.concurrent.atomic.AtomicInteger

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.google.gson.Gson
import com.redis.RedisClient
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

import scala.collection.mutable.HashMap
import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.parsing.json.JSONObject

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

  var jobIdToSize = new HashMap[Int,Long]()
  var jobIdToIp = new HashMap[Int,String]()

  val CHUNK_SIZE = 125

  var rabbitMQ = new RabbitMQ

//  val r = new RedisClient("localhost", 6379)


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
    var hash = job.getHash

    var currentEnd = start
    var currentStart = start
    while (toLong(currentEnd) < toLong(end)){
      currentEnd = currentStart
      currentEnd = nextStr(currentEnd,2)
      currentEnd = nextStr(currentEnd,2)

      if(toLong(currentEnd)>toLong(end)) currentEnd = end

      println("chunk: "+currentStart+" -> "+currentEnd)
      //      add to queue
      rabbitMQ.addJob(new Job(jobId, currentStart, currentEnd, hash))

      currentStart = nextStr(currentEnd,1)





    }

  }

  // (fake) async database query api
  def fetchItem(jobId: Long): Future[Option[Job]] = Future {
    jobs.find(o => o.getJobId == jobId)
  }



  def saveJob(job: dispatchedJob): Future[Done] = {
    jobs = job match {
            case dispatchedJob(hash) => {
              var currentId = jobId.getAndIncrement()
              currentId = currentId.intValue()
              val totalSize = findSize("A","AAA")
              jobIdToSize += (currentId -> totalSize)
              println(jobIdToSize)
              val j = Job(currentId, "A", "AAA", hash)

              println("Job Created: "+currentId)
              val thread = new Thread{
                override def run: Unit = {
                  splitAndQueue(j)
                }
              }
              thread.start()
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

              // TODO: Save Client ip to job id to ip
              var rip = "111"
              extractClientIP{ip=>
                rip = ip.toString()
                println("HELLOOOO")
              complete("done")
              }
              extractClientIP
              println("IP: "+ rip)

              val saved: Future[Done] = saveJob(job)
              onComplete(saved) { done =>
                complete("Job created")
              }
            }
          }
        } ~
        post {
          path("status"){
            println(requestEntityPresent)
            entity(as[String]) { entity =>
              println("---------")
              val content = entity.substring(1,entity.length()-1)
              val resultArray = content.split(",")
              val id = resultArray(0).toInt
              val isFound = resultArray(1)
              val rs = resultArray(2)
              jobIdToSize(id) -= CHUNK_SIZE
              println(jobIdToSize(id))

              if(isFound == "true"){

                // TODO: send to client
//                val clientIp = jobIdToIp(id)
//                 val post = new HttpPost(clientIp + "/answer")
//                post.setHeader("Content-type", "application/json")
//                val jsonString = new Gson().toJson(rs)
//                post.setEntity(new StringEntity(jsonString))

              }

              complete("")
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