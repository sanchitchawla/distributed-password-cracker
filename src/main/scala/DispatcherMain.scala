import scala.concurrent.duration._
/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain {

  def main(args: Array[String]): Unit = {

    val receiver = "http://localhost.com:8082/createJob"
    val startRange = "AA"
    val endRange = "9999A"
    val hash = "icMez.omxHuqU"

    val dispatcher: Dispatcher = new Dispatcher(receiver,hash)

    dispatcher.send()

    // Should auto ping every 5 seconds
    var deadline = 5.seconds.fromNow
    while(deadline.isOverdue()) {
      dispatcher.ping()
      deadline = 5.seconds.fromNow
    }
  }

}
