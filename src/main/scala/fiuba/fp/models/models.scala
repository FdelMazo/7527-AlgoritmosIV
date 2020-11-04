package fiuba.fp.models

import doobie.implicits._
import doobie.implicits.javatime._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import doobie.Update0

case class DataSetRow(
                     id: Int,
                     date: LocalDateTime,
                     open: Option[Double],
                     high: Option[Double],
                     low: Option[Double],
                     last: Double,
                     close: Double,
                     diff: Double,
                     curr: String,
                     OVol: Option[Int],
                     Odiff: Option[Int],
                     OpVol: Option[Int],
                     unit: String,
                     dollarBN: Double,
                     dollarItau: Double,
                     wDiff: Double
                     )

object DataSetRow {
  def build_row(row : List[String]) : Option[DataSetRow] = {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");

    row match {
      case List(id, date, open, high, low, last, close, diff, curr, oVol, odiff, opVol, unit, dollarBN, dollarItau, wDiff) => scala.util.Try(DataSetRow(id.toInt, LocalDateTime.parse(date.replace("a.m.", "AM").replace("p.m.", "PM"), formatter), scala.util.Try(open.toDouble).toOption, scala.util.Try(high.toDouble).toOption, scala.util.Try(low.toDouble).toOption, last.toDouble, close.toDouble, diff.toDouble, curr, scala.util.Try(oVol.toInt).toOption, scala.util.Try(odiff.toInt).toOption, scala.util.Try(opVol.toInt).toOption, unit, dollarBN.toDouble, dollarItau.toDouble, wDiff.toDouble)).toOption
      case _ => None
    }
  }

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
}
