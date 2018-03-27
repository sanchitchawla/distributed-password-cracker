import redis.clients.jedis.Jedis;

public class Redis {

    private Jedis jedis;

    public Redis(String ip){

        this.jedis= new Jedis(ip);
    }

    public boolean isDone(Integer id){

        return this.jedis.get(id.toString()).equals("DONE");
    }


    public static void main(String[] args) {
        Redis r = new Redis("0.0.0.0");
        r.jedis.set("1","ASBDNFGKJH");


        System.out.println(r.jedis.get("1"));
        System.out.println(r.jedis.get("2"));
        System.out.println(r.isDone(1));
        r.jedis.set("1","DONE");
        System.out.println(r.isDone(1));
    }

}