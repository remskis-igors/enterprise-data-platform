import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File

class JsonParsingSpec extends AnyFlatSpec with Matchers {

  private val spark: SparkSession = SparkSession.builder()
    .appName("JsonParsingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint/JsonParsingSpec")
    .getOrCreate()

  import spark.implicits._

  "JSON Parser" should "handle all valid data type combinations" in {
    val jsonData = Seq(
      """{"id": "123", "value": 42.5, "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": 99.9, "event_time": "2023-01-02T12:00:00Z"}"""
    )

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val inputDF = spark.createDataset(jsonData).toDF("json_str")
      .select(from_json(col("json_str"), schema).as("data"))
      .select("data.*")

    inputDF.schema shouldBe schema
    val rows = inputDF.collect()
    rows.length should be(2)
    rows(0).getString(0) should be("123")
    rows(0).getDouble(1) should be(42.5)
    rows(0).getTimestamp(2).toInstant.toString should be("2023-01-01T10:15:30Z")
  }

  it should "handle missing required fields" in {
    val jsonData = Seq(
      """{"id": "123", "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": 99.9}"""
    )

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val inputDF = spark.createDataset(jsonData).toDF("json_str")
      .select(from_json(col("json_str"), schema).as("data"))
      .select("data.*")
      .na.drop()

    val rows = inputDF.collect()
    rows.length should be(0) // Все строки должны быть отброшены
  }

  it should "handle invalid data types" in {
    val jsonData = Seq(
      """{"id": "123", "value": "invalid", "event_time": "2023-01-01T10:15:30Z"}"""
    )

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val inputDF = spark.createDataset(jsonData).toDF("json_str")
      .select(from_json(col("json_str"), schema).as("data"))
      .select("data.*")
      .na.drop()

    val rows = inputDF.collect()
    rows.length should be(0) // Неверное значение должно исключаться
  }

  it should "handle malformed JSON strings" in {
    val jsonData = Seq(
      """{malformed json}"""
    )

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val inputDF = spark.createDataset(jsonData).toDF("json_str")
      .select(from_json(col("json_str"), schema).as("data"))
      .select("data.*")
      .na.drop()

    val rows = inputDF.collect()
    rows.length should be(0)
  }

  it should "handle empty and null values" in {
    val jsonData = Seq(
      """{}""",
      null
    )

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val inputDF = spark.createDataset(jsonData).toDF("json_str")
      .select(from_json(col("json_str"), schema).as("data"))
      .select("data.*")
      .na.drop()

    val rows = inputDF.collect()
    rows.length should be(0)
  }
}
