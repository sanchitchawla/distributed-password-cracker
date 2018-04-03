import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

/**
  * Created by sanch on 25-Jan-18.
  */
class DispatcherMain {

  class Ping () {
    override def toString = "ping"
  }

  def pingClient(receiver: String) = {
    val json = new Ping()
    val Json = new Gson().toJson(json)

    val post = new HttpPost("http://"+receiver + "/ping")

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


  def run(args: Array[String]): Unit = {

    val host = args(0)

    val receiver = "http://"+host+"/createJob"

    val hash = args(1)
//    val hash = "icXR6gWTOXkzU"
    val dispatcher: Dispatcher = new Dispatcher(receiver, hash)

    dispatcher.serv()

    //  dispatcher.send()
    println("Sent")


  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext
  import ExecutionContext.Implicits.global

  val system = akka.actor.ActorSystem("system")
  system.scheduler.schedule(0 seconds, 5 seconds)(pingClient(receiver))


  }
}
