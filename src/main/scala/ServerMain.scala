/**
  * Created by sanch on 29-Jan-18.
  */
object ServerMain extends App{

  val server = new Server("local")
  println(server.receive())

}
