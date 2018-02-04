import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import java.util.concurrent.atomic.AtomicInteger

import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpServer {
  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var jobId: AtomicInteger = new AtomicInteger(1)

  var jobs: List[Job] = List(Job(1,"A", "AAA", "hduiwgfe"))

  final case class dispatchedJob(startString: String, endString: String, hash: String)

  // formats for unmarshalling and marshalling
  implicit val jobFormat = jsonFormat4(Job)
  implicit val dispatchedJobFormat = jsonFormat3(dispatchedJob)

  // (fake) async database query api
  def fetchItem(jobId: Long): Future[Option[Job]] = Future {
    jobs.find(o => o.getJobId == jobId)
  }

  def saveJob(job: dispatchedJob): Future[Done] = {
    jobs = job match {
      case dispatchedJob(startString, endString, hash) => Job(jobId.getAndIncrement(), startString, endString, hash) :: jobs
      case _            => jobs
    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("getJob" / LongNumber) { id =>
          // there might be no item for a given id
          val maybeJob: Future[Option[Job]] = fetchItem(id)

          onSuccess(maybeJob) {
            case Some(job) => complete(job)
            case None       => complete(StatusCodes.NotFound)
          }
        }
      } ~
        post {
          path("createJob") {
            entity(as[dispatchedJob]) { job =>
              val saved: Future[Done] = saveJob(job)
              onComplete(saved) { done =>
                complete("Job created")
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8082)
    println(s"Server online at http://0.0.0.0:8082/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}