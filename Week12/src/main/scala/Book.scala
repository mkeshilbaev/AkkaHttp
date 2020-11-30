case class Book(id: String,
                title: Option[String],
                author:String,
                category:String,
                quantity: Int)


case class CreateBook(title: Option[String],
                     author: String,
                      category: String,
                     quantity: Int)