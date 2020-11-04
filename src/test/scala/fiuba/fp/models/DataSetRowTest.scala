package fiuba.fp.models

import java.time.LocalDateTime

import fiuba.fp.FpTpSpec
import fiuba.fp.Run.split

class DataSetRowTest extends FpTpSpec {

    "A DataSetRow" should "represent a row" in {

        val row = DataSetRow(12,LocalDateTime.now(),None,Option(12.3),Some(13.6),89.9,123.45,-1213.0,"D", Some(12),None,None,"TONS",123.34,567.23,1234.5)

        row.id shouldBe 12
    }

    "The split method" should "return an array of strings splitted by a comma" in {
        val str = "abc,c,d"
        split(str) shouldBe List("abc","c","d")
    }

    "The split method" should "return an array with an empty string when trying to split an empty string" in {
        val str = ""
        split(str) shouldBe List("")
    }

    "The split method" should "return a list with just the string, if there is no comma" in {
        val str = "abc"
        split(str) shouldBe List("abc")
    }

    "The split method" should "return an empty string and the rest for a string starting with comma" in {
        val str = ",abc"
        split(str) shouldBe List("", "abc")
    }

    "The split method" should "return an string and then an empty string for a string ending with comma" in {
        val str = "abc,"
        split(str) shouldBe List("abc", "")
    }

    "The split method" should "place an empty string where there are two commas together" in {
        val str = "a,,c"
        split(str) shouldBe List("a", "", "c")
    }

}
