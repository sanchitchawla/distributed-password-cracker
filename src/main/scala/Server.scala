import scalaj.http.{Http, HttpResponse}

/**
  * Created by sanch on 29-Jan-18.
  */
class Server {

  def receive(): String ={
    val response = Http("localhost").param("try", "try2").asString
    response.body
  }

  def distributeChunks(): Unit ={
    val responseBody = receive()
  }
}
