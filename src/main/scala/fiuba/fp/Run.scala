package fiuba.fp

import cats.effect._
import fiuba.fp.utils.Utils

object Run extends App {
  if(args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

  val acquire = IO {scala.io.Source.fromFile(args(0))}
  Resource.fromAutoCloseable(acquire).use(
    source => IO {
      source.getLines().foreach {
        line => println (s"${Utils.split(line)}")
      }
    }
  ).unsafeRunSync()
}
