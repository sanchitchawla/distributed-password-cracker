
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

import scala.util.Try
object RabbitMQ extends App {

  def rabbitIsRunning(host: String = "172.17.0.2", port: Int = 5672): Boolean =
    Try {
      val factory: ConnectionFactory = new ConnectionFactory
      factory.setHost(host)
      factory.setPort(port)
      val conn: Connection           = factory.newConnection
      val channel: Channel           = conn.createChannel
      channel.close()
      conn.close()
    }.isSuccess

  println(rabbitIsRunning())
  println("ehuheueh")
}