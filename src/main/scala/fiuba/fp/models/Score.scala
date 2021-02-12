package fiuba.fp.models

import doobie.implicits._

case class Score(
  hash: Int,
  score: Option[Double])
