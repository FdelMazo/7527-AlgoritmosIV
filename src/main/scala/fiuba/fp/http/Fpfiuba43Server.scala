package fiuba.fp.http

import cats.effect.{ ContextShift, Timer, ConcurrentEffect }
import cats.effect.IO.{ contextShift, ContextSwitch }
import fiuba.fp.services.{ HealthCheckImpl, ScoreServiceImpl }
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Fpfiuba43Server {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], contextShift: ContextShift[F]): Stream[F, Nothing] = {

    val healthCheck = new HealthCheckImpl[F]("entergroupname")
    val scoreService = new ScoreServiceImpl[F]()
    val httpApp = Fpfiuba43Routes.routes[F](healthCheck, scoreService).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
