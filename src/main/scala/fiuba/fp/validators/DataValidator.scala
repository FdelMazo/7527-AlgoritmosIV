package fiuba.fp.validators

import scala.util.{Failure, Success, Try}

final case class ValidationException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)


sealed trait DataValidator
case object DateValidator extends DataValidator
case object DoubleValidator extends DataValidator
case object IntValidator extends DataValidator
case class MaxLengthValidator(maxLength: Int) extends DataValidator

object DataValidator {
  val DATE_PATTERN = "dd/MM/yyyy hh:mm:ss"
  def validate(validator: DataValidator, data: String): Try[Any] = validator match {
    case DateValidator => {
      val fixedData = data.replaceFirst("a\\.m\\.", "m")
      val format = new java.text.SimpleDateFormat(DATE_PATTERN)
      Try(format.parse(fixedData))
    }
    case DoubleValidator => Try(data.toDouble)
    case IntValidator => Try(data.toInt)
    case MaxLengthValidator(maxLength) => if (data.size <= maxLength)  Success(data) else Failure(ValidationException("maxLength failed"))
  }
}
