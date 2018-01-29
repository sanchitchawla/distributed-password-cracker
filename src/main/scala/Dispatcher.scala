import scalaj.http.{Http, HttpOptions}

/**
  * Created by sanch on 25-Jan-18.
  */
class Dispatcher(receiver: String, startRange: String, endRange: String, hash:String) {

  def send(): Unit ={
    val result =
      Http(receiver)
      .postForm(Seq("startRange" -> startRange, "endRange" -> endRange, "hash" -> hash))
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    println(result)
  }

}
