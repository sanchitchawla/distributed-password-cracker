import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class WorkerJava {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.17.0.2");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //TODO: Remove this and test

                String message = new String(body, "UTF-8");
                String[] each = message.split(",");
                Job job = new Job(1, each[0],each[1] ,each[2] );

                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(job);
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
        // Check redis if job is done

        Jedis jedis = new Jedis("172.17.0.3");
        jedis.set("1", "Not Done");
        System.out.println(jedis.get("1"));
        String startRange = job.getStartString();
        String endRange = job.getEndString();
        String hash = job.getHash();

        System.out.println("Cracking");
        ParallelCracker.cracker(startRange, endRange, hash);

    }
}