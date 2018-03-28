import com.google.gson.Gson;
import com.rabbitmq.client.*;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.HttpClients;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import java.util.concurrent.TimeoutException;




public class WorkerJava {

    private Jedis redis;

    private String serverAddr;

    private final String TASK_QUEUE_NAME = "task_queue";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    HttpClient httpclient = HttpClients.createDefault();



    public WorkerJava(String serverAddr, String rabbitAddr, String redisAddr) throws IOException, TimeoutException {

        this.serverAddr = serverAddr;
        redis = new Jedis(redisAddr);

        factory = new ConnectionFactory();
        factory.setHost(rabbitAddr);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);
    }

    public void listen() throws IOException {



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

    public void doWork(Job job) {
        String startRange = job.getStartString();
        String endRange = job.getEndString();
        String hash = job.getHash();
        Integer id = job.getJobId();
        String found = redis.get(id.toString());
        System.out.println(found);
        if(found.equals("DONE")) return;

        System.out.println("Cracking");
        ParallelCracker parallelCracker = new ParallelCracker();
        boolean isFound = parallelCracker.cracker(startRange, endRange, hash);
        String pass;
        if(isFound){
            pass = parallelCracker.getResult();
            redis.set(id.toString(),"DONE");

        }
        else{
            pass = " ";
        }


        String s = String.valueOf(isFound);
        String t = pass;
        String jsonString = new Gson().toJson(id+","+s+","+t);
        System.out.println(jsonString);
        HttpPost httppost = new HttpPost("http://"+serverAddr+"/status");
        httppost.setHeader("Content-type", "application/json");
        try{
            httppost.setEntity(new StringEntity(jsonString));
            httpclient.execute(httppost);
        }
        catch (Exception e){
            System.out.println("error: "+e);
        }

    }


}