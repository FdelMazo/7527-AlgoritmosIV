package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import fiuba.fp.utils.Utils

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

  List(DataSetRow.drop, DataSetRow.create).foreach { q => q.transact(transactor).unsafeRunSync }

  val acquire = IO { scala.io.Source.fromFile(args(0)) }

  Resource.fromAutoCloseable(acquire).use(
    source => IO {
      source.getLines().drop(1).foreach {
        line =>
          val row : Option[DataSetRow] = DataSetRow.build_row(Utils.split(line));
          row match {
            case None => None
            case Some(i) => DataSetRow.insert_row(i).run.transact(transactor).unsafeRunSync
          }
      }
    }).unsafeRunSync
}
