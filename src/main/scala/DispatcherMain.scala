/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain {

  val receiver = ""
  val startRange = "AA"
  val endRange = "9999A"
  val hash = "icMez.omxHuqU"

  val dispatcher: Dispatcher = new Dispatcher(receiver, startRange, endRange, hash)

  dispatcher.send()
}
