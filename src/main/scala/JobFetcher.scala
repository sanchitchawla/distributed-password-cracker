
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


object JobFetcher {

  // get job from queue and spawn working instance

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val server = "http://192.168.1.138:8082/worker"

  var status = "NOT_DONE"

  def  getJobfromQueue():Unit = {

    // TODO: get a job from Queue
    

    val job = Job(1, "BA", "CA", "wertgv34")
    status = "NOT_DONE"

    // TODO: check whether that jobId is done (from redis)

    // if not done
    val startString = job.getStartString
    val endString = job.getEndString
    val hash = job.getHash

    new Worker(startString, endString, hash)

    status = "DONE"

    // When worker is done
    postStatus()

  }

  def postStatus(): Unit ={

    val json = new StatusValue(status)
    val Json = new Gson().toJson(json)

    println(Json)

    // create an HttpPost object
    val post = new HttpPost(server)

    // set the Content-type
    post.setHeader("Content-type", "application/json")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(Json))

    // send the post request
    val response = HttpClientBuilder.create().build().execute(post)

    // print the response headers
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))



  }

  class StatusValue(status: String){
    override def toString = "status, " + status
  }


  def main(args: Array[String]): Unit = {


//        postStatus()
    //    getJobfromQueue()

    val route: Route =

    // WHEN SERVER PINGS
      get {
        pathPrefix("ping") {
          // returning status
          complete(status)
        }
      }
    // TODO: POST REQUEST from SERVER when this job IS DONE AND SEND STATUS BACK


    val bindingFuture = Http().bindAndHandle(route,"0.0.0.0", 8084)
    println(s"Worker online at http://0.0.0.0:8082/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
