import java.io.{BufferedReader, InputStreamReader}

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest, RequestEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import com.google.gson.Gson

import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
  * Created by sanch on 25-Jan-18.
  */

class Dispatcher(receiver: String, hash:String) {

  def send(): Unit ={

//    val json = new HashValue(hash)
//    val Json = new Gson().toJson(json)
//
//    println(Json)
//
//    // create an HttpPost object
//    val post = new HttpPost(receiver)
//
//    // set the Content-type
//    post.setHeader("Content-type", "application/json")
//
//    // add the JSON as a StringEntity
//    post.setEntity(new StringEntity(Json))
//
//    // send the post request
//    val response = HttpClientBuilder.create().build().execute(post)
//
//
//    // print the response headers
//    println("--- HEADERS ---")
//    response.getAllHeaders.foreach(arg => println(arg))

    val formData = Await.result(Marshal(FormData(json)).to[RequestEntity], Duration.Inf)
    val content = for {
      request <- Marshal(formData).to[RequestEntity]
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = receiver, entity = request))
      entity <- Unmarshal(response.entity).to[String]
    } yield entity



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



