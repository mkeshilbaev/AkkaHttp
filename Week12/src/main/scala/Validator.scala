trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object CreateBookValidator extends Validator[CreateBook] {
  def validate(createBook: CreateBook): Option[ApiError] =
    if (createBook.title.isEmpty)
      Some(ApiError.emptyTitleField)
    else
      None
}