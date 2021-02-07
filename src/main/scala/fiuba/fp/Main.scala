package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import cats.implicits._
import org.apache.spark.ml.{ Pipeline, PipelineModel }
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.sql.types.{ DateType, DoubleType, IntegerType, StringType, StructField, StructType }
import org.apache.spark.ml.regression.RandomForestRegressor
import org.apache.spark.ml.feature.{ StringIndexer, VectorAssembler }
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.SQLImplicits
import org.jpmml.sparkml.PMMLBuilder
import org.jpmml.model.metro.MetroJAXBUtil
import java.io.{ FileOutputStream, OutputStream }

import fiuba.fp.rand.LCG
import org.apache.spark.rdd.RDD

object Main extends App {

  if (args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val spark = SparkSession.builder()
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  val sparkContext = spark.sparkContext

  val host = scala.util.Try(args(1)).toOption match {
    case Some(h) => h
    case _ => "localhost"
  }

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://$host/fiuba",
    "fiuba", "fiuba")

  val transaction = DataSetRow.select()
    .compile
    .toList
    .transact(transactor)

  val schema = StructType(
    StructField("id", IntegerType, nullable = false) ::
      StructField("date", DateType, nullable = false) ::
      StructField("open", DoubleType, nullable = false) ::
      StructField("high", DoubleType, nullable = false) ::
      StructField("low", DoubleType, nullable = false) ::
      StructField("last", DoubleType, nullable = false) ::
      StructField("close", DoubleType, nullable = false) ::
      StructField("diff", DoubleType, nullable = false) ::
      StructField("curr", StringType, nullable = false) ::
      StructField("OVol", IntegerType, nullable = false) ::
      StructField("Odiff", IntegerType, nullable = false) ::
      StructField("OpVol", IntegerType, nullable = false) ::
      StructField("unit", StringType, nullable = false) ::
      StructField("dollarBN", DoubleType, nullable = false) ::
      StructField("dollarItau", DoubleType, nullable = false) ::
      StructField("wDiff", DoubleType, nullable = false) ::
      StructField("hash", IntegerType, nullable = false) ::
      Nil)

  val lcg0 = LCG(117)

  val s1 = LCG.randStream(lcg0)

  val dataTagged = for {
    a <- s1 zip transaction.unsafeRunSync
    if a._2.productIterator.exists(_ == None) == false
  } yield {
    println(a._2)
    (a._2, a._1 <= 0.3)
  }

  val dataMap: Map[Boolean, Stream[(DataSetRow, Boolean)]] = dataTagged.groupBy(item => item._2)
  val (testStream, trainStream) = (dataMap.get(true), dataMap.get(false))
  val testDF = testStream.map(s => s.map(t => t._1)).get.toDF
  val trainDF = trainStream.map(s => s.map(t => t._1)).get.toDF

  val unit_indexer = new StringIndexer()
    .setInputCol("unit")
    .setOutputCol("unit_idx")
    .setHandleInvalid("error")

  val curr_indexer = new StringIndexer()
    .setInputCol("curr")
    .setOutputCol("curr_idx")
    .setHandleInvalid("error")

  val cols = Array(
    "id",
    "open",
    "high",
    "low",
    "last",
    "diff",
    "curr_idx",
    "OVol",
    "Odiff",
    "OpVol",
    "unit_idx",
    "dollarBN",
    "dollarItau",
    "wDiff")

  val assembler: VectorAssembler = new VectorAssembler()
    .setInputCols(cols)
    .setOutputCol("features")
    .setHandleInvalid("error")

  val randomForestRegressor = new RandomForestRegressor()
    .setLabelCol("close")
    .setSeed(117)
    .setNumTrees(10)
    .setFeatureSubsetStrategy("auto");

  val stages = Array(curr_indexer, unit_indexer, assembler, randomForestRegressor);

  val pipeline = new Pipeline().setStages(stages);

  val pipelineModel: PipelineModel = pipeline.fit(trainDF)

  val pred = pipelineModel.transform(testDF)

  val evaluator = new RegressionEvaluator()
    .setLabelCol("close")
    .setPredictionCol("prediction")
    .setMetricName("mae")

  val mae = evaluator.evaluate(pred)
  println(s"test MAE: $mae")

  val pmml = new PMMLBuilder(schema, pipelineModel).build(); //PMML type

  val os: OutputStream = new FileOutputStream("cosoide.pmml");
  MetroJAXBUtil.marshalPMML(pmml, os);

  spark.close()
}
