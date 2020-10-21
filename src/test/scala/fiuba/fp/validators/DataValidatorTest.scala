package fiuba.fp.validators

import fiuba.fp.FpTpSpec

class DataValidatorTest extends FpTpSpec {

  "Doubles" can "be naturals" in {
    val zero = "0";
    assert(DataValidator.validate(DoubleValidator, zero));

    val one = "1";
    assert(DataValidator.validate(DoubleValidator, one));
  }

  it can "be negatives" in {
    val minusOne = "-1";
    assert(DataValidator.validate(DoubleValidator, minusOne));
    val minus = "-";
    assert(!DataValidator.validate(DoubleValidator, minus));
  }

  it can "only have a single dot between numbers" in {
    val startDot = ".14";
    val middleDot = "3.14";
    val endDot = "3.";
    assert(!DataValidator.validate(DoubleValidator, startDot));
    assert(DataValidator.validate(DoubleValidator, middleDot));
    assert(!DataValidator.validate(DoubleValidator, endDot));
  }

  it can "not be alphanumeric" in {
    val word = "foo";
    assert(!DataValidator.validate(DoubleValidator, word));
  }

  "Integer" can "be natural" in {
    val zero = "0";
    assert(DataValidator.validate(IntValidator, zero));

    val one = "1";
    assert(DataValidator.validate(IntValidator, one));
  }

  it can "be negative" in {
    val minusOne = "-1";
    assert(DataValidator.validate(IntValidator, minusOne));
    val minus = "-";
    assert(!DataValidator.validate(IntValidator, minus));
  }

  it must "not have a single dot between numbers" in {
    val startDot = ".14";
    val middleDot = "3.14";
    val endDot = "3.";
    assert(!DataValidator.validate(IntValidator, startDot));
    assert(!DataValidator.validate(IntValidator, middleDot));
    assert(!DataValidator.validate(IntValidator, endDot));
  }

  it must "not be alphanumeric" in {
    val word = "foo";
    assert(!DataValidator.validate(IntValidator, word));
  }

  "Date" should "have have a date format" in {
    val validDate = "31/12/2004 11:59:59 a.m.";
    assert(DataValidator.validate(DateValidator, validDate));
    val invalidDate = "foo";
    assert(!DataValidator.validate(DateValidator, invalidDate));
  }

  "Strings" can "have a max length" in {
    val validator = MaxLengthValidator(3);
    assert(DataValidator.validate(validator, "fo"));
    assert(DataValidator.validate(validator, "foo"));
    assert(!DataValidator.validate(validator, "fooo"));
  }
}
