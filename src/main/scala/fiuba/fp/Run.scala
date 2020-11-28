package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import cats.implicits._

object Run extends App {
  if (args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val host = scala.util.Try(args(1)).toOption match {
    case Some(h) => h
    case _ => "localhost"
  }

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://$host/fiuba",
    "fiuba", "fiuba")

  def processRow(row: DataSetRow): Unit = {
    println(row)
  }

  val description = DataSetRow.select()
    .map(processRow)
    .compile
    .toList
    .transact(transactor)     // IO[List[String]]
    .unsafeRunSync    // List[String]

  println("App end")
}
