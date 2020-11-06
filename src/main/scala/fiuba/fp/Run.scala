package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import fiuba.fp.utils.Utils
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

  List(DataSetRow.drop, DataSetRow.create).foreach { q => q.run.transact(transactor).unsafeRunSync }

  val acquire = IO { scala.io.Source.fromFile(args(0)) }

  Resource.fromAutoCloseable(acquire).use(
    source => source.getLines()
      .drop(1)
      .grouped(512)
      .map(gr =>
        gr.map(Utils.split)
          .flatMap(DataSetRow.build_row)
          .map(DataSetRow.insert_rows.run)
          .toList
          .sequence
          .map(_.sum)
          .transact(transactor)
      )
      .toList
      .sequence
  ).unsafeRunSync
}
