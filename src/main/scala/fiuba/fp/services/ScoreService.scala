package fiuba.fp.services

import cats.effect._
import cats.implicits._
import doobie.ExecutionContexts
import doobie.hikari._
import doobie.implicits._
import fiuba.fp.models.{Score, ScoreMessage, InputRow}


trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Async](implicit contextShift: ContextShift[F]) extends ScoreService[F] {
  override def score(data: InputRow): F[ScoreMessage] = {

    val transactor: Resource[F, HikariTransactor[F]] =
      for {
        ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
        be <- Blocker[F]    // our blocking EC
        xa <- HikariTransactor.newHikariTransactor[F](
          "org.postgresql.Driver",                        // driver classname
          "jdbc:postgresql://localhost/fiuba",   // connect URL
          "fiuba",                                   // username
          "fiuba",                                     // password
          ce,                                     // await connection here
          be                                      // execute JDBC operations here
        )
      } yield xa

    transactor.use { xa =>
      for {
          scoreRow <- sql"""select * from Score where hash_code = ${data.hash}""".query[Score].option.transact(xa)
          score <- scoreRow match {
            case Some(x) => x.score.get.pure[F]
            case _ => sql"""insert into Score values (${data.hash}, ${data.score})""".update.run.transact(xa).map(_ => data.score)
          }
      } yield ScoreMessage(score.toString())
    }
  }
}











