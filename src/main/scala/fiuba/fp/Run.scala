package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import fiuba.fp.utils.Utils
import cats.implicits._
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType,StringType, DateType}
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.jpmml.PMMLBuilder

object Run extends App {
/*
  if (args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

  implicit val cs = IO.contextShift(ExecutionContext.global)

   val host = scala.util.Try(args(1)).toOption match {
    case Some(h) => h
    case _ => "localhost"
  }
  
  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://$host/fiuba",
    "fiuba", "fiuba")

  List(DataSetRow.drop, DataSetRow.create).foreach { q => q.run.transact(transactor).unsafeRunSync }

  val acquire = IO { scala.io.Source.fromFile(args(0)) }

  Resource.fromAutoCloseable(acquire).use(
    source => source.getLines()
      .drop(1)
      .grouped(512)
      .map(gr =>
        gr.map(Utils.split)
          .flatMap(DataSetRow.build_row)
          .map(DataSetRow.insert_rows.run)
          .toList
          .sequence
          .map(_.sum)
          .transact(transactor)
      )
      .toList
      .sequence
  ).unsafeRunSync
*/
val spark = SparkSession.builder()
    .master("local[*]")
    .getOrCreate()

val schema = StructType(
      StructField("id", IntegerType, nullable=false) ::
      StructField("date", DateType, nullable=false) ::
      StructField("open", DoubleType, nullable=true) ::
      StructField("high", DoubleType, nullable=true) ::
      StructField("low", DoubleType, nullable=low) ::
      StructField("last", DoubleType, nullable=false) ::
      StructField("close", DoubleType, nullable=false) ::
      StructField("diff", DoubleType, nullable=false) ::
      StructField("curr", StringType, nullable=false) ::
      StructField("OVol", IntegerType, nullable=low) ::
      StructField("Odiff", IntegerType, nullable=low) ::
      StructField("OpVol", IntegerType, nullable=low) ::
      StructField("unit", StringType, nullable=false) ::
      StructField("dollarBN", DoubleType, nullable=false) ::
      StructField("dollarItau", DoubleType, nullable=false) ::
      StructField("wDiff", DoubleType, nullable=false) ::
      StructField("hash", IntegerType nullable=false) ::
      Nil
  )

val trainDF: DataFrame = spark.read.format("csv")
    .option("header", value = true)
    .option("delimiter", ",")
    .option("mode", "DROPMALFORMED")
    .schema(schema)
    .load("train.csv")
    .cache()

val categoricals = df.dtypes.filter (_._2 == "StringType") map (_._1)

val indexers = categoricals.map (
  c => new StringIndexer().setInputCol(c).setOutputCol(s"${c}_idx")
)

val encoders = categoricals.map (
  c => new OneHotEncoder().setInputCol(s"${c}_idx").setOutputCol(s"${c}_enc")
)

val randomForestRegressor = new RandomForestRegressor()
    .setSeed(117)
    .setNumTrees(100)
    .setFeatureSubsetStrategy("auto")


val stages = Array(indexers,encoders,randomForestRegressor)

val pipeline = new Pipeline().setStages(stages)

val pipelineModel: PipelineModel = pipeline.fit(trainDF)

PMML pmml = new PMMLBuilder(schema, pipelineModel).build();

  val os: OutputStream = new FileOutputStream("cosoide.pmml");
  MetroJAXBUtil.marshalPMML(pmml, os);
}
