
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import com.google.gson.Gson
/**
  * Created by sanch on 25-Jan-18.
  */

// Left with PING
class Dispatcher(hash:String) {

  def send(): Unit ={

    // convert it to a JSON string
    val Json = new Gson().toJson("hash" -> hash)

    // create an HttpPost object
    val post = new HttpPost("http://localhost:8082/post")

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




}



