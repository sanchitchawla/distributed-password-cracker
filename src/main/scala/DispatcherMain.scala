import scala.concurrent.duration._
/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain  extends App{


  val receiver = "http://192.168.1.242:8082/createJob"
  val startRange = "AA"
  val endRange = "9999A"
  val hash = "icMez.omxHuqU"

  val dispatcher: Dispatcher = new Dispatcher(receiver,hash)

  dispatcher.send()
  println("Sent")

  // Should auto ping every 5 seconds
  var deadline = 5.seconds.fromNow
  while(deadline.isOverdue()) {
    dispatcher.ping()
    deadline = 5.seconds.fromNow

  }

}
