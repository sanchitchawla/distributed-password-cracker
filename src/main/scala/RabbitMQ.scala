import io.relayr.amqp._
import scala.concurrent.duration._

object RabbitMQ extends App{

  val connection = ConnectionHolder.builder("amqps://guest:password@host:port")
    .reconnectionStrategy(ReconnectionStrategy.JavaClientFixedReconnectDelay(1 seconds))
    .build()

  val channel = connection.newChannel()

  def consumer(request: Message): Unit = request match {
    case Message.String(string) => println(string)
      // Do job
      // Create new Worker(startRange, endRange, hash)
  }
  val queue = QueueDeclare(Some("workqueue"))
  channel.addConsumer(queue, consumer)

//  channel.send(Exchange.Default.route("queue.name"), Message.String("message"))

}