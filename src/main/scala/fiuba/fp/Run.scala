package fiuba.fp

import cats.effect._


object Run extends App {

<<<<<<< HEAD
    if(args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

    val acquire = IO {scala.io.Source.fromFile(args(0))}
    def split(str: String): Array[String] = {
        return str.split(",")
    }

    Resource.fromAutoCloseable(acquire).use(
      source => IO {
        source.getLines().foreach {
          line => println (s"New line: ${line}")
        }
      }
    ).unsafeRunSync() 
=======
    def split(str: String): List[String] = {
        val seed: List[List[Char]] = List(Nil)

        val listOfStrings: List[List[Char]] = str.foldLeft[List[List[Char]]](seed)((acc, e) => {
            if (e == ',') {
                Nil :: acc
            } else {
                (e :: acc.head.reverse).reverse :: acc.tail
            }
        })
        listOfStrings.map(substr => substr.foldLeft[String]("")((acc, e) => acc + e)).reverse
    }


    val program = IO { println(s"Hello!") }
    
    program.unsafeRunSync() 
>>>>>>> Add a FP version of split
}
