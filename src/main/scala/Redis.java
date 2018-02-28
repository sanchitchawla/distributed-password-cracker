import redis.clients.jedis.Jedis;

public class Redis {

    private Jedis jedis;

    public Redis(){

        this.jedis= new Jedis("172.17.0.2");
    }

    public boolean isDone(Integer id){

        return this.jedis.get(id.toString()).equals("DONE");
    }

}
