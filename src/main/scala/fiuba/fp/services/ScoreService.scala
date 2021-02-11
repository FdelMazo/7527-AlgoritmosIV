package fiuba.fp.services

import doobie.implicits._
import cats.Applicative
import cats.effect.IO
import cats.implicits._
import doobie.util.transactor.Transactor
import fiuba.fp.Main.{transaction, s1, assembler, args}
import fiuba.fp.models.{DataSetRow, Score, ScoreMessage, InputRow}
import org.jpmml.evaluator.{Evaluator,LoadingModelEvaluatorBuilder}
import org.dmg.pmml.{FieldName}
import org.jpmml.evaluator.{FieldValue,EvaluatorUtil}
import java.io.File
import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._




trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Applicative]() extends ScoreService[F] {
  def PMMLevaluate(data: InputRow): Double = {
    val evaluator = new LoadingModelEvaluatorBuilder().load(new File("/home/nox/repos/fiuba/7527-algoritmos-4/7527-AlgoritmosIV/cosoide.pmml")).build();

    evaluator.verify();

    val dataMap : Map[String, Any] = data.getClass.getDeclaredFields.map(_.getName).zip(data.productIterator.to).toMap
    
    val arguments : Map[FieldName, FieldValue] = (for {
      inputField <- evaluator.getInputFields.asScala.toList
      rawValue <- dataMap.get(inputField.getName.getValue)
    } yield (
      inputField.getName,
      inputField.prepare(rawValue)
    )).toMap

    val results = evaluator.evaluate(arguments.asJava)
    
    0.7
  }

  override def score(data: InputRow): F[ScoreMessage] = {
    implicit val cs = IO.contextShift(ExecutionContext.global)

    val transactor = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      s"jdbc:postgresql://localhost/fiuba",
      "fiuba", "fiuba")

    //val datasetrow = DataSetRow.selectById(data.id)
    //  .compile
    //  .toList
    //  .transact(transactor)
    //  .unsafeRunSync
    //  .head
    /*
    val scorerow = Score.selectByHash(data.hashCode())
      .compile
      .toList
      .transact(transactor).option
      .unsafeRunSync

    val score = scorerow match {
      case Some(h) => h
      case _ => PMMLevaluate(data)
    }
   */
    val score : Double = PMMLevaluate(data)

    ScoreMessage("La la la").pure[F]
  }

}
