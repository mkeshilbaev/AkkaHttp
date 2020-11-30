import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}


trait CategoryRepository {
  def all(): Future[Seq[Category]]
//  def getByTitle(title: String): Future[Seq[Category]]
//  def delete(id: String): Future[Seq[Category]]
  def create(createCategory: CreateCategory): Future[Category]
}

class InMemoryCategoryRepository(category:Seq[Category] = Seq.empty)(implicit  ex:ExecutionContext) extends CategoryRepository {
  private var categories: Vector[Category] = category.toVector

  override def all(): Future[Seq[Category]] = Future.successful(categories)
  //  override def getByTitle(title: String): Future[Seq[Book]] = Future.successful(books.filter(_.title === title))
  //  override def delete(id: Long): Future[Seq[Book]] = Future.successful(books.filter(_.id === id)))

  override def create(createCategory: CreateCategory): Future[Category] =
      Future.successful {
        val category = Category(
          id = UUID.randomUUID().toString,
          title = createCategory.title
        )
        categories = categories :+ category
        category
      }
}
