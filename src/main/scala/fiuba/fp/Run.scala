package fiuba.fp
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import models._

object Run extends App {

  def split(str : String) : List[String] = {

    val seed : List[List[Char]] = List(Nil)

    val listOfStrings : List[List[Char]] = str.foldRight[List[List[Char]]](seed)((e, acc) => {
      if (e == ',') {
        Nil :: acc
      } else {
        (e :: acc.head) :: acc.tail
      }
    })
    listOfStrings.map(substr => substr.foldLeft[String]("")((acc, e) => acc + e))
  }

  def build_row(row : List[String]) : Option[DataSetRow] = {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");

    row match {
      case List(id, date, open, high, low, last, close, diff, curr, oVol, odiff, opVol, unit, dollarBN, dollarItau, wDiff) => scala.util.Try(DataSetRow(id.toInt, LocalDateTime.parse(date.replace("a.m.", "AM").replace("p.m.", "PM"), formatter), scala.util.Try(open.toDouble).toOption, scala.util.Try(high.toDouble).toOption, scala.util.Try(low.toDouble).toOption, last.toDouble, close.toDouble, diff.toDouble, curr, scala.util.Try(oVol.toInt).toOption, scala.util.Try(odiff.toInt).toOption, scala.util.Try(opVol.toInt).toOption, unit, dollarBN.toDouble, dollarItau.toDouble, wDiff.toDouble)).toOption
      case _ => None
    }
  }

  def insert_row(row : DataSetRow) : Update0 = {
    sql"""insert into datasetrow values (
     ${row.id},
     ${row.date},
     ${row.open},
     ${row.high},
     ${row.low},
     ${row.last},
     ${row.close},
     ${row.diff},
     ${row.curr},
     ${row.OVol},
     ${row.Odiff},
     ${row.OpVol},
     ${row.unit},
     ${row.dollarBN},
     ${row.dollarItau},
     ${row.wDiff}
      )""".update
  }

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

  val drop =
    sql"""
        DROP TABLE IF EXISTS datasetrow
      """.update.run

  val create =
    sql"""
        CREATE TABLE datasetrow (
          id   integer,
          date timestamp,
          open float,
          high float,
          low float,
          last float,
          close float,
          diff float,
          curr varchar,
          OVol integer,
          Odiff integer,
          Opvol integer,
          unit varchar,
          dollarBN float,
          dollarItau float,
          wDiff float
        )
      """.update.run

  List(drop, create).foreach { q => q.transact(transactor).unsafeRunSync }

  val acquire = IO { scala.io.Source.fromFile(args(0)) }

  Resource.fromAutoCloseable(acquire).use(
    source => IO {
      source.getLines().drop(1).foreach {
        line =>
          val row : Option[DataSetRow] = build_row(split(line));
          row match {
            case None => None
            case Some(i) => insert_row(i).run.transact(transactor).unsafeRunSync
          }
      }
    }).unsafeRunSync
}
