package fiuba.fp.http

import cats.effect.{IO, Sync}
import cats.implicits._
import fiuba.fp.models.{InputRow}
import fiuba.fp.services.{HealthCheck, ScoreService}
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

case class User(id: String, date: String, last: String)

object Fpfiuba43Routes {
  def routes[F[_]: Sync](h: HealthCheck[F], scoreService: ScoreService[F]): HttpRoutes[F] = {
    Router[F] (
      "/health-check" -> healthCheckRoutes(h),
    "/score" -> scoreRoutes(scoreService)
    )
  }

  def healthCheckRoutes[F[_]: Sync](h: HealthCheck[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root =>
        for{
          healthCheck <- h.healthCheck
          resp <- Ok(healthCheck.asJson)
        } yield resp
    }
  }

  def scoreRoutes[F[_]: Sync](scoreService: ScoreService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    implicit val decoder = jsonOf[F, InputRow]
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        println(req.as[InputRow])
        for{
          data <- req.as[InputRow]
          score <- scoreService.score(data)
          resp <- Ok(score.asJson)
        } yield resp
    }
  }

}