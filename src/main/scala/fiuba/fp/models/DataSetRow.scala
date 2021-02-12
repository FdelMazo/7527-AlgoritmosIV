package fiuba.fp.models

import doobie._
import doobie.implicits._
import cats.effect._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.implicits.javasql._
import java.time.LocalDateTime
import java.sql.Date
import java.time.format.DateTimeFormatter

import doobie.Update0
import doobie.Update
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class DataSetRow(
                       id: Int,
                       date: java.sql.Date,
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
                       wDiff: Double,
                       hash: Int
                     )

object DataSetRow {
  def select() : fs2.Stream[doobie.ConnectionIO, DataSetRow] = {
    sql"""
    select *, 0 from fptp.dataset
    """.query[DataSetRow]
      .stream
      //.take (100)
  }
}
