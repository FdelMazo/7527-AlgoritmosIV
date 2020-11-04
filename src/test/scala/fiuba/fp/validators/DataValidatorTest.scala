package fiuba.fp.validators

import fiuba.fp.FpTpSpec

class DataValidatorTest extends FpTpSpec {

  "Doubles" can "be naturals" in {
    val zero = "0"
    assert(DataValidator.validate(DoubleValidator, zero).isSuccess)

    val one = "1"
    assert(DataValidator.validate(DoubleValidator, one).isSuccess)
  }

  it can "be negatives" in {
    val minusOne = "-1"
    assert(DataValidator.validate(DoubleValidator, minusOne).isSuccess)
    val minus = "-"
    assert(DataValidator.validate(DoubleValidator, minus).isFailure)
  }

  it can "only have a single dot" in {
    assert(DataValidator.validate(DoubleValidator, ".14").isSuccess)
    assert(DataValidator.validate(DoubleValidator, "3.14").isSuccess)
    assert(DataValidator.validate(DoubleValidator, "31.").isSuccess)
    assert(DataValidator.validate(DoubleValidator, "3.1.4").isFailure)
  }

  it can "not be alphanumeric" in {
    val word = "foo"
    assert(DataValidator.validate(DoubleValidator, word).isFailure)
  }

  "Integer" can "be natural" in {
    val zero = "0"
    assert(DataValidator.validate(IntValidator, zero).isSuccess)

    val one = "1"
    assert(DataValidator.validate(IntValidator, one).isSuccess)
  }

  it can "be negative" in {
    val minusOne = "-1"
    assert(DataValidator.validate(IntValidator, minusOne).isSuccess)
    val minus = "-"
    assert(DataValidator.validate(IntValidator, minus).isFailure)
  }

  it must "not have a single dot between numbers" in {
    val startDot = ".14"
    val middleDot = "3.14"
    val endDot = "3."
    assert(DataValidator.validate(IntValidator, startDot).isFailure)
    assert(DataValidator.validate(IntValidator, middleDot).isFailure)
    assert(DataValidator.validate(IntValidator, endDot).isFailure)
  }

  it must "not be alphanumeric" in {
    val word = "foo"
    assert(DataValidator.validate(IntValidator, word).isFailure)
  }

  "Date" should "have have a date format" in {
    val validDate = "31/12/2004 11:59:59 a.m."
    assert(DataValidator.validate(DateValidator, validDate).isSuccess)
    val invalidDate = "foo"
    assert(DataValidator.validate(DateValidator, invalidDate).isFailure)
  }

  "Strings" can "have a max length" in {
    val validator = MaxLengthValidator(3)
    assert(DataValidator.validate(validator, "fo").isSuccess)
    assert(DataValidator.validate(validator, "foo").isSuccess)
    assert(DataValidator.validate(validator, "fooo").isFailure)
  }
}
