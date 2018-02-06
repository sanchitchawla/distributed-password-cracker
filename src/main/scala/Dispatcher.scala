import java.io.{BufferedReader, InputStreamReader}

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import com.google.gson.Gson
/**
  * Created by sanch on 25-Jan-18.
  */

class Dispatcher(receiver: String, hash:String) {

  def send(): Unit ={

    // convert it to a JSON string
    val Json = new Gson().toJson("hash" -> hash)

    // create an HttpPost object
    val post = new HttpPost(receiver)

    // set the Content-type
    post.setHeader("Content-type", "application/json")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(Json))

    // send the post request
    val response = HttpClientBuilder.create().build().execute(post)
    // print the response headers
//    println("--- HEADERS ---")
//    response.getAllHeaders.foreach(arg => println(arg))

  }

  def ping() = {
    // Ping
    val command = "ping " + receiver
    val p : Process = Runtime.getRuntime.exec(command)
    val inputStreamReader = new BufferedReader(new InputStreamReader(p.getInputStream))

    var s: String = ""
    while ((s = inputStreamReader.readLine()) != null) {
      println(s)
    }
  }




}



