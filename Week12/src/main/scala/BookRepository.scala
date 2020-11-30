import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}


trait BookRepository {
  def all(): Future[Seq[Book]]
  def getByTitle(title: String): Future[Seq[Book]]
  def delete(id: String): Future[Seq[Book]]
//  def update(name: String): Future[Seq[Book]]
  def create(createBook:CreateBook): Future[Book]
}

class InMemoryBookRepository(book:Seq[Book] = Seq.empty)(implicit  ex:ExecutionContext) extends BookRepository {
  private var books: Vector[Book] = book.toVector

  override def all(): Future[Seq[Book]] = Future.successful(books)
  override def getByTitle(title: String): Future[Seq[Book]] = Future.successful(books.filter(_.title == title))
  override def delete(id: String): Future[Seq[Book]] = Future.successful(books.filterNot(_.id == id))

  override def create(createBook: CreateBook): Future[Book] =
    if (createBook.author.startsWith("e1")) Future.failed {
      new Exception("e1")
    }
    else
      Future.successful {
        val book = Book(
          id = UUID.randomUUID().toString,
          title = createBook.title,
          author = createBook.author,
          category = createBook.category,
          quantity = createBook.quantity
        )
        books = books :+ book
        book
      }
}