import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File
import java.util

/**
 * Unit tests for Spark Streaming transformations.
 */
class ScalaServiceSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private val spark: SparkSession = SparkSession.builder()
    .appName("SparkStreamingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint/ScalaServiceSpec")
    .getOrCreate()

  import spark.implicits._

  override def beforeAll(): Unit = {
    super.beforeAll()
    val checkpointDir = new File("target/checkpoint/ScalaServiceSpec")
    if (checkpointDir.exists()) {
      checkpointDir.delete()
    }
  }

  override def afterAll(): Unit = {
    spark.stop()
    super.afterAll()
  }

  "JSON message parsing" should "correctly extract fields from valid JSON" in {
    val jsonData = Seq(
      """{"id": "123", "value": 42.5, "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": 99.9, "event_time": "2023-01-02T12:00:00Z"}"""
    )

    val inputDF = spark.createDataset(jsonData).toDF("json_str")

    val parsedDF = inputDF
      .select(from_json(col("json_str"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", TimestampType)
      ).as("data"))
      .select("data.*")

    val rows = parsedDF.collect()
    rows.length should be(2)

    rows(0).getString(0) should be("123")
    rows(0).getDouble(1) should be(42.5)
    rows(0).getTimestamp(2).toInstant.toString should be("2023-01-01T10:15:30Z")

    rows(1).getString(0) should be("456")
    rows(1).getDouble(1) should be(99.9)
    rows(1).getTimestamp(2).toInstant.toString should be("2023-01-02T12:00:00Z")
  }

  "Kafka message processing" should "convert messages to the correct format" in {
    val kafkaData = Seq(
      (null, """{"id": "789", "value": 33.3, "event_time": "2023-01-03T15:45:30Z"}""".getBytes())
    )

    val schema = StructType(Seq(
      StructField("key", BinaryType, true),
      StructField("value", BinaryType, true)
    ))

    val rowList = new util.ArrayList[Row]()
    kafkaData.foreach { case (key, value) => rowList.add(Row(key, value)) }

    val kafkaDF = spark.createDataFrame(rowList, schema)

    val messages = kafkaDF
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", TimestampType)
      ).as("data"))
      .select("data.*")

    val rows = messages.collect()
    rows.length should be(1)

    rows(0).getString(0) should be("789")
    rows(0).getDouble(1) should be(33.3)
    rows(0).getTimestamp(2).toInstant.toString should be("2023-01-03T15:45:30Z")
  }

  "The full pipeline" should "process messages end-to-end" in {
    val testData = Seq(
      """{"id": "abc", "value": 123.45, "event_time": "2023-01-05T08:30:00Z"}""",
      """{"id": "def", "value": 67.89, "event_time": "2023-01-05T09:00:00Z"}"""
    )

    import org.apache.spark.sql.execution.streaming.MemoryStream
    implicit val sqlContext = spark.sqlContext
    val stream = MemoryStream[String]
    stream.addData(testData)

    val jsonSchema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val messages = stream.toDF()
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), jsonSchema).as("data"))
      .select("data.*")

    val query = messages.writeStream
      .format("memory")
      .queryName("results")
      .outputMode(OutputMode.Append())
      .option("checkpointLocation", "target/checkpoint/full_pipeline")
      .start()

    query.processAllAvailable()

    val results = spark.table("results").collect()
    results.length should be(2)

    results(0).getString(0) should be("abc")
    results(0).getDouble(1) should be(123.45)
    results(0).getTimestamp(2).toInstant.toString should be("2023-01-05T08:30:00Z")

    results(1).getString(0) should be("def")
    results(1).getDouble(1) should be(67.89)
    results(1).getTimestamp(2).toInstant.toString should be("2023-01-05T09:00:00Z")

    query.stop()
  }
}
