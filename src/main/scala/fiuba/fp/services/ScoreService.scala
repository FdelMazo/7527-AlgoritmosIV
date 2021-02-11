package fiuba.fp.services

import cats.Applicative
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fiuba.fp.models.{DataSetRow, Score, ScoreMessage, InputRow}
import java.io.File
import org.dmg.pmml.{FieldName, PMML}
import org.jpmml.evaluator.{FieldValue,EvaluatorUtil,LoadingModelEvaluatorBuilder}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Applicative]() extends ScoreService[F] {
    def PMMLevaluate(data: InputRow): String = {
      val evaluator = new LoadingModelEvaluatorBuilder().load(new File("/home/nox/repos/fiuba/7527-algoritmos-4/7527-AlgoritmosIV/cosoide.pmml")).build();

      evaluator.verify();

      //val dataMap : Map[String, Any] = (data.productElementNames zip data.productIterator).toMap

      val dataMap : Map[String, Any] = Map(
        "id" -> 158,
        "date" -> "2020-12-02T14:49:15.841609",
        "last" -> 0.0,
        "diff" -> 0.0,
        "curr"-> "D",
        "unit" -> "TONS",
        "dollarBN"-> 2.919,
        "dollarItau"-> 2.91,
        "wDiff"-> -148.0
      )

      val arguments : Map[FieldName, FieldValue] = (for {
        inputField <- evaluator.getInputFields.asScala.toList
        rawValue <- dataMap.get(inputField.getName.getValue)
      } yield (
        inputField.getName,
        inputField.prepare(rawValue)
      )).toMap

      val results = evaluator.evaluate(arguments.asJava)
      return results.toString

    }

  override def score(data: InputRow): F[ScoreMessage] = {
    implicit val cs = IO.contextShift(ExecutionContext.global)

    val transactor = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      s"jdbc:postgresql://localhost/fiuba",
      "fiuba", "fiuba"
    )

    val scorerow: Option[Score] = Score.selectByHash(data.hashCode)
      .compile
      .toList
      .transact(transactor)
      .unsafeRunSync
      .headOption

    val score = scorerow match {
      case Some(h) => h.score
      case _ => PMMLevaluate(data)
    }

    ScoreMessage(score.toString).pure[F]
  }

}
