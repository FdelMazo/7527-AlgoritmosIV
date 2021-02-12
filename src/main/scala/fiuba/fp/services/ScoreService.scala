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
import org.jpmml.evaluator.InputField

trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Applicative]() extends ScoreService[F] {
    def PMMLevaluate(data: InputRow): String = {
      val evaluator = new LoadingModelEvaluatorBuilder().load(new File("cosoide.pmml")).build();

      evaluator.verify();

      //val dataMap : Map[String, Any] = (data.productElementNames zip data.productIterator).toMap

      val dataMap : Map[String, Any] = Map(
        "id"-> 158,
        "date"-> "2020-12-02T14->49->15.841609",
        "last"-> 0.0,
        "close"-> 148.0,
        "diff"-> 0.0,
        "curr"-> "D",
        "unit"-> "TONS",
        "dollarBN"-> 2.919,
        "dollarItau"-> 2.91,
        "wDiff"-> -148.0
      )

      val input_fields: List[InputField] = evaluator.getInputFields.asScala.toList
      val input_fields_namevals: List[String] = input_fields.map(_.getName).map(_.getValue)
      val input_fields_mapping : Map[String, InputField] = (input_fields_namevals zip input_fields).toMap 
    
      // for each field_name, field_value in dataMap:
      //     field = input_fields_mapping.get(field_name)
      //     yield (field.getName, field.prepare(field_value))
      
      val arguments : Map[FieldName, FieldValue] = for {
        kv <- dataMap
        if input_fields_mapping.contains(kv._1)
      } yield {
        (input_fields_mapping.apply(kv._1).getName, input_fields_mapping.apply(kv._1).prepare(kv._2))
      }

      val results = evaluator.evaluate(arguments.asJava)
      
      val resultRecord = EvaluatorUtil.decodeAll(results)

      return s"""
      input_fields_mapping: ${input_fields_mapping.toString}
      data map: ${dataMap.toString}
      arguments: ${arguments.toString},
      output fields: ${evaluator.getOutputFields.toString},
      target fields: ${evaluator.getTargetFields.toString},
      input fields: ${evaluator.getInputFields.toString},
      result record: ${resultRecord.toString},
      results: ${results.toString}"""

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
