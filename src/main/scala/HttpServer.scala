import java.util.concurrent.ConcurrentLinkedQueue

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.google.gson.Gson
import com.redis.RedisClient
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{HttpClientBuilder, HttpClients}
import redis.clients.jedis.Jedis

import scala.collection.mutable.HashMap
import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.parsing.json.JSONObject

object HttpServer {
  // needed to run the route
  implicit val system = ActorSystem()

  println(s"remote-addr-hdr: ${system.settings.config.getString("akka.http.server.remote-address-header")}")

  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var jobId: AtomicInteger = new AtomicInteger(1)

  var isBusy = new AtomicBoolean(false)

  var workerToJob = new HashMap[String,Job]()
  var jobs: List[Job] = List()

  final case class dispatchedJob(hash: String)

  // formats for unmarshalling and marshalling
  implicit val jobFormat = jsonFormat4(Job)
  implicit val dispatchedJobFormat = jsonFormat1(dispatchedJob)

  var jobIdToSize = new HashMap[Int,Long]()
  var jobIdToIp = new HashMap[Int,String]()
  var jobIdToResult = new HashMap[Int,String]()

  val CHUNK_SIZE = 125
  var REDIS_HOST = "0.0.0.0"
  var RABBIT_HOST = "0.0.0.0"

  var rabbitMQ: RabbitMQ = null

  var redis: Jedis = null

  val conQ = new ConcurrentLinkedQueue[Job]

  val needToStop = new AtomicBoolean(false)



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
    while (!needToStop.get() && (toLong(currentEnd) < toLong(end))){
      currentEnd = currentStart
      currentEnd = nextStr(currentEnd,2)
      currentEnd = nextStr(currentEnd,2)

      if(toLong(currentEnd)>toLong(end)) currentEnd = end

//      println("chunk: "+currentStart+" -> "+currentEnd)
      //      add to queue
      rabbitMQ.addJob(new Job(jobId, currentStart, currentEnd, hash))

      currentStart = nextStr(currentEnd,1)





    }

  }

  def saveJob(job: dispatchedJob, ip: String, currentId: Int): Future[Done] = {
    jobs = job match {
            case dispatchedJob(hash) => {
              val totalSize = findSize("A","AAAAAA")
              jobIdToSize += (currentId -> totalSize)
              jobIdToIp += (currentId -> ip)
              println(jobIdToSize)
              println(jobIdToIp)
              val j = Job(currentId, "A", "AAAAAA", hash)

              redis.set(currentId.toString,"NOT_DONE")

              println("Job Created: "+currentId)
              conQ.add(j)
              println(isBusy.get())
              if(!isBusy.getAndSet(true)) {
                val newJ = conQ.poll()
                val thread = new Thread {
                  override def run: Unit = {
                    splitAndQueue(newJ)
                  }
                }
                thread.start()
              }
              else {
                println(currentId+"Waiting++++++++++++++++++++++++++++++++++++++++++++++++")
              }



              j :: jobs

            }
            case _            => jobs

    }
    Future { Done }
  }

  def main(args: Array[String]) {

    RABBIT_HOST = args(0)

    REDIS_HOST = args(1)

    rabbitMQ = new RabbitMQ(RABBIT_HOST)

    redis = new Jedis(REDIS_HOST)

    val route: Route =
      get {
        pathPrefix("getJob" / IntNumber) { id =>
          // there might be no item for a given id
          val rs = jobIdToResult(id)
          if(rs == ""){
            complete("running")
          }
          else {
            complete(rs)
          }
        }
      } ~
        post {
          path("createJob") {
            extractClientIP {clientIp =>
              entity(as[dispatchedJob]) { job =>

                // TODO: Save Client ip to job id to ip

                val incomingIp = clientIp.toOption.map(_.getHostAddress).getOrElse("unknown")

                println("IP: " + incomingIp)
                println("Job: " + job)

                var currentJobId = jobId.getAndIncrement().toInt


                val saved: Future[Done] = saveJob(job,incomingIp,currentJobId)
                onComplete(saved) { done =>
                  complete(currentJobId.toString)
                }
            }
          }
          }
        } ~
        post {
          path("status"){
            entity(as[String]) { entity =>
//              println("---------")
              val content = entity.substring(1,entity.length()-1)
              val resultArray = content.split(",")
              val id = resultArray(0).toInt
              val isFound = resultArray(1)
              var rs = resultArray(2)
              jobIdToSize(id) -= CHUNK_SIZE
//              println(jobIdToSize(id),isFound)

              if(isFound == "true"){
                  println(id,rs)
//                set chunk remaining to zero

                jobIdToSize(id) = 0
                println("Job size: "+jobIdToSize(id))
                needToStop.set(true)
                println("Interupt+++++++++")
//                Thread.sleep(5000)

                rabbitMQ.clearQueue()
                println(conQ.size())
                needToStop.set(false)
                if(!conQ.isEmpty){
                  val newJ = conQ.poll()
                  val thread = new Thread {
                    override def run: Unit = {
                      splitAndQueue(newJ)
                    }
                  }
                  thread.start()
                }
                else{
                  isBusy.set(false)
                }

                // TODO: send to client
                println("Prepared send result: "+rs)
                val clientIp = jobIdToIp(id)
                println(clientIp)

                val post = new HttpPost("http://"+clientIp + ":8091/receive")
                println(post)
                post.setHeader("Content-type", "application/json")
                val jsonString = new Gson().toJson(rs)

                post.setEntity(new StringEntity(jsonString))
                val httpclient = HttpClients.createDefault
                httpclient.execute(post)
              }
              else if(jobIdToSize(id) <= 0){
                rs = "404 password not found"
                val clientIp = jobIdToIp(id)
                println(clientIp)

                val post = new HttpPost("http://"+clientIp + ":8091/receive")
                println(post)
                post.setHeader("Content-type", "application/json")
                val jsonString = new Gson().toJson(rs)

                post.setEntity(new StringEntity(jsonString))
                val httpclient = HttpClients.createDefault
                httpclient.execute(post)
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