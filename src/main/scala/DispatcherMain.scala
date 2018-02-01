/**
  * Created by sanch on 25-Jan-18.
  */
object DispatcherMain {

  def main(args: Array[String]): Unit = {

    // input: serverip, port, hash

    val receiver = "http://192.168.1.138:8082/item/80"
    val startRange = "AA"
    val endRange = "9999A"
    val hash = "icMez.omxHuqU"

    val dispatcher: Dispatcher = new Dispatcher(receiver, startRange, endRange, hash)

    dispatcher.send()
  }

}
