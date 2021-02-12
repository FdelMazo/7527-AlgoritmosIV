package fiuba.fp

import cats.effect.{ ExitCode, IO, IOApp }
import fiuba.fp.http.Fpfiuba43Server

object API extends IOApp {

  def run(args: List[String]): IO[ExitCode] =

    Fpfiuba43Server.stream[IO].compile.drain.as(ExitCode.Success)

}