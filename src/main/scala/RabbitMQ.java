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
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
    }

    public void addJob(Job job) throws IOException, TimeoutException {

        channel.basicPublish("", TASK_QUEUE_NAME,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                job.toString().getBytes());

        System.out.println(" [x] Sent '" + job.getHash() + "'");

//        channel.close();
//        connection.close();
    }

    public void clearQueue() throws Exception {
        channel.queuePurge(TASK_QUEUE_NAME);
    }

//    public static void main(String[] args) throws IOException, TimeoutException {
//        RabbitMQ rabbitMQ = new RabbitMQ();
//        addJob(new Job(1, "A", "AAA", "ic39fhj"));
//        addJob(new Job(2, "A", "AAA", "ic4wq4tfttzU2"));
//
////        WorkerJava workerJava = new WorkerJava();
////        workerJava.listen();
//    }

}