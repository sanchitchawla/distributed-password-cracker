import redis.clients.jedis.Jedis;

public class Redis {

    private Jedis jedis;

    public Redis(){

        this.jedis= new Jedis("172.17.0.2");
    }

    public boolean isDone(Integer id){

        return this.jedis.get(id.toString()).equals("DONE");
    }


    public static void main(String[] args) {
        Redis r = new Redis();
        r.jedis.append("1","BODAS");

        System.out.println(r.isDone(1));
    }

}