import java.io.{BufferedReader, InputStreamReader}

import HttpServer.{jobIdToResult, system}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest, RequestEntity}
import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import com.google.gson.Gson

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

/**
  * Created by sanch on 25-Jan-18.
  */

class Dispatcher(receiver: String, hash:String) {

  implicit val system = ActorSystem()

  println(s"remote-addr-hdr: ${system.settings.config.getString("akka.http.server.remote-address-header")}")

  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def send(): Unit ={

    val json = new HashValue(hash)
    val Json = new Gson().toJson(json)

    println(Json)

    // create an HttpPost object
    val post = new HttpPost(receiver)

    // set the Content-type
    post.setHeader("Content-type", "application/json")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(Json))

    // send the post request
    val response = HttpClientBuilder.create().build().execute(post)


    // print the response headers
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))

//    val formData = Marshal(FormData(Map("hash"->hash))).to[RequestEntity]
//    val content = for {
//      request <- Marshal(formData).to[RequestEntity]
//      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = receiver, entity = request))
//      entity <- Unmarshal(response.entity).to[String]
//    } yield entity
//

  }

  def serv(): Route = {
    val route: Route =
      post {
        path("receive") {
          entity(as[String]) { pass =>
            println("---------")

            println("Found pass " + pass)
            complete("Received")
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8091)
    println(s"Server online at http://0.0.0.0:8091/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
    route
  }

  def ping(): Unit = {
    // Ping
    val command = "ping " + receiver
    val p : Process = Runtime.getRuntime.exec(command)
    val inputStreamReader = new BufferedReader(new InputStreamReader(p.getInputStream))

    var s: String = ""
    while ((s = inputStreamReader.readLine()) != null) {
      println(s)
    }
  }

  class HashValue (var hash: String) {
    override def toString = "hash" + ", " + hash
  }
}



