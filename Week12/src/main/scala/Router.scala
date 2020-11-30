import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import domain.CreateUser
import io.circe.generic.auto._
import repos.UserRepository
import scala.concurrent.ExecutionContext


trait Router {
  def route: Route
}

class MyRouter(val bookRepository: BookRepository, val userRepository: UserRepository)(implicit system: ActorSystem[_], ex: ExecutionContext)
  extends Router
    with Directives
    with ValidatorDirectives
    with BookDirectives {


  def route = {
    path("users") {
      concat(
      pathEndOrSingleSlash {

        concat(
          get {
            complete(userRepository.all())
          },
          post {
            entity(as[CreateUser]) { createUser =>
              handleWithGeneric(userRepository.create(createUser)) { user =>
                complete(user)
              }
            }
          })
      },

      path("books") {
        concat(
          pathEndOrSingleSlash {
            concat(
              get {
                complete(bookRepository.all())
              },
              post {
                entity(as[CreateBook]) { createBook =>
                  validateWith(CreateBookValidator)(createBook) {
                    handleWithGeneric(bookRepository.create(createBook)) { book =>
                      complete(book)
                    }
                  }
                }
              }
            )
          },
          path("getByTitle") {
            get {
              complete(bookRepository.all())
            }
          },
          path("delete") {
            get {
              complete(bookRepository.all())
            }
          },

          path("hello") {
            get {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
            }
          })
      })
    }
  }
}

