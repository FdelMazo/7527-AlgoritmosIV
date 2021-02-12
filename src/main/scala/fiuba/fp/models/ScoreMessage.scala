package fiuba.fp.models

import io.circe._
import io.circe.generic.semiauto._

final case class ScoreMessage(score: String)

object ScoreMessage {
  implicit val encoder: Encoder[ScoreMessage] = deriveEncoder
}
