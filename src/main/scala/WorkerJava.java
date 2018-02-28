import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WorkerJava {

    private static final String TASK_QUEUE_NAME = "task_queue";
    private static ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;

    public WorkerJava() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("172.17.0.3");
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);
    }

    public static void listen() throws IOException {

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String[] each = message.split(",");
                System.out.println(message);

                // The last index
                Integer id = Integer.parseInt(each[0].substring(each.length, each.length + 1));
                String startRange = each[1];
                String endRange = each[2];
                String hash = each[3].substring(0, each[3].length() - 1);

                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(new Job(id, startRange, endRange, hash));
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        boolean autoAck = false;
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
    }

    private static void doWork(Job job) {
        String startRange = job.getStartString();
        String endRange = job.getEndString();
        String hash = job.getHash();

        System.out.println("Cracking");
        ParallelCracker.cracker(startRange, endRange, hash);

    }
}