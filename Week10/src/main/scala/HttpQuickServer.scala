import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scala.concurrent.ExecutionContext



object HttpQuickServer {

  def main(args: Array[String]): Unit = {
    val log: Logger = LoggerFactory.getLogger(getClass)

    val behavior = Behaviors.setup[Nothing] {
      context =>
        def route = {
          path("nums") {
            post {
              entity(as[List[String]]) { nums =>
                log.info(nums.toString())
                complete(nums)
              }
            }
          }
        }

        startHttpServer(route)(context.system, context.executionContext)
        Behaviors.empty
    }
    val system = ActorSystem[Nothing](behavior, "my-system")
  }

  def startHttpServer(route: Route)(implicit system: ActorSystem[_], ex: ExecutionContext): Unit = {
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
  }
}



