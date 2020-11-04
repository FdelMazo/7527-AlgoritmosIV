package fiuba.fp.utils

final case object Utils {
  def split(str: String): List[String] = {

    val seed: List[List[Char]] = List(Nil)

    val listOfStrings: List[List[Char]] = str.foldRight[List[List[Char]]](seed)((e, acc) => {
      if (e == ',') {
        Nil :: acc
      }
      else {
        (e :: acc.head) :: acc.tail
      }
    })
    listOfStrings.map(substr => substr.foldLeft[String]("")((acc, e) => acc + e))
  }
}
