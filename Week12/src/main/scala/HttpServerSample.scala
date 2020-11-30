import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import domain.User
import org.slf4j.{Logger, LoggerFactory}
import repos.{InMemoryUserRepository, UserRepository}

import scala.util.Try

object HttpServerSample {

  def main(args: Array[String]): Unit = {
    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val books: Seq[Book] = Seq(
        Book("1", Some("title1"), "author1", "category1", 1),
        Book("2", Some("title2"), "author2", "category2", 2)
      )

      val users: Seq[User] = Seq(
        User("1", "name1", "login1", "password1")
      )

      val bookRepository = new InMemoryBookRepository(books)(context.executionContext)
      val userRepository = new InMemoryUserRepository(users)(context.executionContext)
      val router = new MyRouter(bookRepository, userRepository)(context.system, context.executionContext)

//      val host = "0.0.0.0"
//      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(9000)

      Server.startHttpServer(router.route, "localhost", 8080)(context.system, context.executionContext)
      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}
