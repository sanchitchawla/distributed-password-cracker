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


    public RabbitMQ(String host) throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(5672);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
    }

    public void addJob(Job job) throws IOException, TimeoutException {

        channel.basicPublish("", TASK_QUEUE_NAME,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                job.toString().getBytes());

    }

    public void clearQueue() throws Exception {
        channel.queuePurge(TASK_QUEUE_NAME);
    }


}