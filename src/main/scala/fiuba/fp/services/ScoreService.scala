package fiuba.fp.services

import doobie.implicits._
import cats.Applicative
import cats.effect.IO
import cats.implicits._
import doobie.util.transactor.Transactor
import fiuba.fp.Main.{transaction, s1, assembler, args}
import fiuba.fp.models.{DataSetRow, Score, ScoreMessage, InputRow}

import scala.concurrent.ExecutionContext

trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Applicative]() extends ScoreService[F] {
  override def score(data: InputRow): F[ScoreMessage] = {
    implicit val cs = IO.contextShift(ExecutionContext.global)

    val transactor = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      s"jdbc:postgresql://localhost/fiuba",
      "fiuba", "fiuba")

    val datasetrow = DataSetRow.selectById(data.id)
      .compile
      .toList
      .transact(transactor)
      .unsafeRunSync
      .head

    val scorerow = Score.selectByHash(datasetrow.hash)
      .compile
      .toList
      .transact(transactor)
      .unsafeRunSync
      .head

    val score = scorerow.score match {
      case Some(h) => h
      case _ => "computame, forro"
    }

    ScoreMessage(score.toString).pure[F]
  }

}
