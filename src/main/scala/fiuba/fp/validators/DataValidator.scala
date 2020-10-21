package fiuba.fp.validators

import java.util.Locale

sealed trait DataValidator
case object DateValidator extends DataValidator
case object DoubleValidator extends DataValidator
case object IntValidator extends DataValidator
case class MaxLengthValidator(maxLength: Int) extends DataValidator

object DataValidator {
  val DATE_PATTERN = "dd/MM/yyyy hh:mm:ss"
  def validate(validator: DataValidator, data: String): Boolean = validator match {
    case DateValidator => {
      val fixedData = data.replaceFirst("a\\.m\\.", "m")
      val format = new java.text.SimpleDateFormat(DATE_PATTERN)
      try {
        format.parse(fixedData)
        true
      } catch {
        case _: Throwable => false
      }
    }
    case DoubleValidator => data.matches("-?([0-9]+[.])?[0-9]+")
    case IntValidator => data.matches("-?[0-9]+")
    case MaxLengthValidator(maxLength) => data.size <= maxLength
  }
}
