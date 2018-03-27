import scala.concurrent.duration._
/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain  extends App{

  val receiver = "http://0.0.0.0:8082/createJob"
  val startRange = "AA"
  val endRange = "9999A"
  val hash = "ic4wq4tfttzU2"

  val dispatcher: Dispatcher = new Dispatcher(receiver,hash)

  dispatcher.send()
  println("Sent")

  dispatcher.serv()



  // Should auto ping every 5 seconds
  var deadline = 5.seconds.fromNow
  while(deadline.isOverdue()) {
    dispatcher.ping()
    deadline = 5.seconds.fromNow
  }

}
