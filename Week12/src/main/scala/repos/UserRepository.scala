package repos

import java.util.UUID

import domain.{CreateUser, User}

import scala.concurrent.{ExecutionContext, Future}


trait UserRepository {
  def all(): Future[Seq[User]]
  def create(createUser: CreateUser): Future[User]

}

class InMemoryUserRepository(user:Seq[User] = Seq.empty)(implicit  ex:ExecutionContext) extends UserRepository {
  private var users: Vector[User] = user.toVector

  override def all(): Future[Seq[User]] = Future.successful(users)

  override def create(createUser: CreateUser): Future[User] =
    if (createUser.name.startsWith("e1")) Future.failed {
      new Exception("e1")
    }
    else
      Future.successful {
        val user = User(
          id = UUID.randomUUID().toString,
          name = createUser.name,
          login = createUser.login,
          password = createUser.password,
        )
        users = users :+ user
        user
      }
}