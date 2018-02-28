import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    private static final String TASK_QUEUE_NAME = "task_queue";
    private static ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;

    public RabbitMQ() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("172.17.0.3");
        factory.setPort(5672);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
    }

    public static void addJob(Job job) throws IOException, TimeoutException {

        channel.basicPublish("", TASK_QUEUE_NAME,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                job.toString().getBytes());

        System.out.println(" [x] Sent '" + job.getHash() + "'");

        channel.close();
        connection.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        RabbitMQ rabbitMQ = new RabbitMQ();
        addJob(new Job(1, "A", "AAA", "ic39fhj"));

        WorkerJava workerJava = new WorkerJava();
        workerJava.listen();
    }

}