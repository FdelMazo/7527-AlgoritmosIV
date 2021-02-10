package fiuba.fp.services

import cats.Applicative
import cats.implicits._
import fiuba.fp.models.ScoreMessage

trait ScoreService[F[_]] {
  def score: F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Applicative]() extends ScoreService[F] {
  override def score: F[ScoreMessage] =
    ScoreMessage("Some score").pure[F]
}
