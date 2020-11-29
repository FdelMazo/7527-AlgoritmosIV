package fiuba.fp
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext
import cats.effect._
import models._
import cats.implicits._
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType,StringType, DateType}
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler, OneHotEncoder}
import org.jpmml.sparkml.{PMMLBuilder} // donde esta PMML?
import org.jpmml.model.metro.MetroJAXBUtil
import java.io.{FileOutputStream, OutputStream}

object Run extends App {

  if (args.length < 1) IO.raiseError(new IllegalArgumentException("Falta archivo de entrada"))

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val spark = SparkSession.builder()
        .master("local[*]")
        .getOrCreate()
  val sparkContext = spark.sparkContext

  val host = scala.util.Try(args(1)).toOption match {
    case Some(h) => h
    case _ => "localhost"
  }

  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://$host/fiuba",
    "fiuba", "fiuba")

  def processRow(row: DataSetRow): DataSetRow = {
    println(row)
    row
  }

  val description = DataSetRow.select()
    .map(processRow)
    .compile
    .toList
    .transact(transactor)

  val dataset : List[DataSetRow] = description.unsafeRunSync

  val rdd = sparkContext.makeRDD[DataSetRow](dataset)
  val schema = StructType(
      StructField("id", IntegerType, nullable=false) ::
      StructField("date", StringType, nullable=false) :: // si, si, ya se
      StructField("open", DoubleType, nullable=false) ::
      StructField("high", DoubleType, nullable=false) ::
      StructField("low", DoubleType, nullable=false) ::
      StructField("last", DoubleType, nullable=false) ::
      StructField("label", DoubleType, nullable=false) :: // cierre
      StructField("diff", DoubleType, nullable=false) ::
      StructField("curr", StringType, nullable=false) ::
      StructField("OVol", IntegerType, nullable=false) ::
      StructField("Odiff", IntegerType, nullable=false) ::
      StructField("OpVol", IntegerType, nullable=false) ::
      StructField("unit", StringType, nullable=false) ::
      StructField("dollarBN", DoubleType, nullable=false) ::
      StructField("dollarItau", DoubleType, nullable=false) ::
      StructField("wDiff", DoubleType, nullable=false) ::
      //StructField("hash", IntegerType, nullable=false) ::
      Nil
  )

  val dataFrame = spark.createDataFrame(rdd)

  dataFrame.show(5) // TODO:remove

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
  .setNumTrees(100)
  .setFeatureSubsetStrategy("auto");

val stages = Array(curr_indexer, unit_indexer, assembler, randomForestRegressor);

val pipeline = new Pipeline().setStages(stages);

val pipelineModel: PipelineModel = pipeline.fit(dataFrame);

/*val pmml = new PMMLBuilder(schema, pipelineModel).build(); //PMML type

val os: OutputStream = new FileOutputStream("cosoide.pmml");
MetroJAXBUtil.marshalPMML(pmml, os);*/

spark.close()

}
