package fiuba.fp

import cats.effect._


object Run extends App {

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

    if(args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

    val acquire = IO {scala.io.Source.fromFile(args(0))}
    Resource.fromAutoCloseable(acquire).use(
      source => IO {
        source.getLines().foreach {
          line => println (s"${split(line)}")
        }
      }
    ).unsafeRunSync() 
}
