package fiuba.fp

import cats.effect._

object Run extends App {

    if(args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

    val acquire = IO {scala.io.Source.fromFile(args(0))}
    
    Resource.fromAutoCloseable(acquire).use(source => IO(println(source.mkString))).unsafeRunSync() 
}
