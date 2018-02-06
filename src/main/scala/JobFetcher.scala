import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

class JobFetcher {

  val server = "http://192.168.1.138:8082/item/80"

  var status = "NOT_DONE"

  def  getJobfromQueue():Unit = {

    // TODO: get a job from Queue

    val job = Job(1, "BA", "CA", "wertgv34")
    done = "NOT_DONE"

    // TODO: check whether that jobId is done (from redis)

    // if not done
    val startString = job.getStartString
    val endString = job.getEndString
    val hash = job.getHash

    new Worker(startString, endString, hash)

    status = "DONE"
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

    class StatusValue(status: Boolean){
      override def toString = "hash, " + status
    }

  }


  def main(args: Array[String]): Unit = {



    //    val receiver = "http://localhost.com:8082/createJob"


  }
}
