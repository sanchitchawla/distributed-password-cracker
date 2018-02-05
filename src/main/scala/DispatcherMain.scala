/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain {

  def main(args: Array[String]): Unit = {

    val receiver = "/createJob"
    val startRange = "AA"
    val endRange = "9999A"
    val hash = "icMez.omxHuqU"

    val dispatcher: Dispatcher = new Dispatcher(receiver,hash)

    dispatcher.send()
  }

}
