package fiuba.fp.models

import doobie._
import doobie.implicits._

import cats.effect._
import doobie.implicits._
import doobie.implicits.javatime._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import doobie.Update0
import doobie.Update

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
  def build_row(row: List[String]): Option[DataSetRow] = {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");

    row match {
      case List(id, date, open, high, low,
      last, close, diff, curr, oVol,
      odiff, opVol, unit, dollarBN, dollarItau,
      wDiff
      ) => scala.util.Try(DataSetRow(id.toInt,
        LocalDateTime.parse(date.replace("a.m.", "AM").replace("p.m.", "PM"), formatter),
        scala.util.Try(open.toDouble).toOption,
        scala.util.Try(high.toDouble).toOption,
        scala.util.Try(low.toDouble).toOption,
        last.toDouble, close.toDouble,
        diff.toDouble, curr,
        scala.util.Try(oVol.toInt).toOption,
        scala.util.Try(odiff.toInt).toOption,
        scala.util.Try(opVol.toInt).toOption,
        unit, dollarBN.toDouble,
        dollarItau.toDouble, wDiff.toDouble)).toOption
      case _ => None
    }
  }


  val drop =
    sql"""
        DROP TABLE IF EXISTS fptp.dataset
      """.update

  val create =
    sql"""
        CREATE SCHEMA IF NOT EXISTS fptp;
        CREATE TABLE fptp.dataset (
          id int PRIMARY KEY,
          date timestamp without time zone NOT NULL,
          open double precision,
          high double precision,
          low double precision,
          last double precision not null,
          close double precision not null,
          dif double precision not null,
          curr character varying(1) NOT NULL,
          o_vol int,
          o_dif int,
          op_vol int,
          unit character varying(4) NOT NULL,
          dollar_BN double precision NOT NULL,
          dollar_itau double precision not null,
          w_diff double precision not null,
          hash_code int not null
        )
      """.update

  def insert_rows(): Update[DataSetRow] = {
    Update[DataSetRow](
      """insert into fptp.dataset values (
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     ?,
     0
    )""")}
  }
