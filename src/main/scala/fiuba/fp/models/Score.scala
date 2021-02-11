package fiuba.fp.models

import doobie.implicits._
case class Score(
                     hash: Int,
                     score: Option[Double],
                   )

object Score {
  def selectByHash(hash: Int) : fs2.Stream[doobie.ConnectionIO, Score] = {
    sql"""
    select * from fptp.scores where hash_code = $hash
    """.query[Score]
      .stream
  }
}
