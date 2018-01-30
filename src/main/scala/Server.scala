
/**
  * Created by sanch on 29-Jan-18.
  */
class Server(url: String) {

  def receive(): String ={
    val result = scala.io.Source.fromURL(url).mkString
    result
  }

  def distributeChunks(): Unit ={
    val responseBody = receive()
  }
}