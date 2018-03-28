
/**
  * Created by sanch on 25-Jan-18.
  */
class DispatcherMain {

  def run(args: Array[String]): Unit = {

    val host = args(0)

    val receiver = "http://"+host+"/createJob"

    val hash = args(1)
//    val hash = "icXR6gWTOXkzU"
    val dispatcher: Dispatcher = new Dispatcher(receiver, hash)

    dispatcher.serv()

    //  dispatcher.send()
    println("Sent")


  }
}
