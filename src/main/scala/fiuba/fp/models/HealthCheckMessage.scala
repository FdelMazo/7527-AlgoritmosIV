package fiuba.fp.models

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class HealthCheckMessage(version: String, maintainer: String)

object HealthCheckMessage {
  implicit val encoder: Encoder[HealthCheckMessage] = deriveEncoder
}