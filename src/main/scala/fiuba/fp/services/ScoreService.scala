package fiuba.fp.services

import cats.Applicative
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fiuba.fp.models.{ DataSetRow, Score, ScoreMessage, InputRow }
import java.io.File
import org.dmg.pmml.{ FieldName, PMML }
import org.jpmml.evaluator.{ FieldValue, EvaluatorUtil, LoadingModelEvaluatorBuilder }
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import org.jpmml.evaluator.InputField
import cats.effect._
import cats.implicits._
import doobie.ExecutionContexts
import doobie.hikari._
import doobie.implicits._
import fiuba.fp.models.{ Score, ScoreMessage, InputRow }

trait ScoreService[F[_]] {
  def score(data: InputRow): F[ScoreMessage]
}

class ScoreServiceImpl[F[_]: Async](implicit contextShift: ContextShift[F]) extends ScoreService[F] {
  def PMMLevaluate(data: InputRow): Double = {
    val evaluator = new LoadingModelEvaluatorBuilder().load(new File("cosoide.pmml")).build();

    evaluator.verify();

    val dataMap: Map[String, Any] = (data.productElementNames zip data.productIterator)
      .filter(_._2 != None)
      .toMap
      .transform(
        (k, v) => v match {
          case x: Option[_] => x.get
          case _ => v
        })

    val input_fields: List[InputField] = evaluator.getInputFields.asScala.toList
    val input_fields_namevals: List[String] = input_fields.map(_.getName).map(_.getValue)
    val input_fields_mapping: Map[String, InputField] = (input_fields_namevals zip input_fields).toMap

    // for each field_name, field_value in dataMap:
    //     field = input_fields_mapping.get(field_name)
    //     yield (field.getName, field.prepare(field_value))

    val arguments: Map[FieldName, FieldValue] = for {
      kv <- dataMap
      if input_fields_mapping.contains(kv._1)
    } yield {
      (input_fields_mapping.apply(kv._1).getName, input_fields_mapping.apply(kv._1).prepare(kv._2))
    }

    val results = evaluator.evaluate(arguments.asJava)

    EvaluatorUtil.decodeAll(results).get("prediction").asInstanceOf[Double]
  }

  override def score(data: InputRow): F[ScoreMessage] = {

    val transactor: Resource[F, HikariTransactor[F]] =
      for {
        ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
        be <- Blocker[F] // our blocking EC
        xa <- HikariTransactor.newHikariTransactor[F](
          "org.postgresql.Driver", // driver classname
          "jdbc:postgresql://localhost/fiuba", // connect URL
          "fiuba", // username
          "fiuba", // password
          ce, // await connection here
          be // execute JDBC operations here
        )
      } yield xa

    PMMLevaluate(data)

    transactor.use { xa =>
      for {
        scoreRow <- sql"""select * from fptp.scores where hash_code = ${data.hash}""".query[Score].option.transact(xa)
        score <- scoreRow match {
          case Some(x) => x.score.get.pure[F]
          case _ => sql"""insert into fptp.scores values (${data.hash}, ${PMMLevaluate(data)})""".update.run.transact(xa).map(_ => PMMLevaluate(data))
        }
      } yield ScoreMessage(score.toString())
    }
  }
}

