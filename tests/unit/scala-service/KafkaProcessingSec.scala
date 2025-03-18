import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File
import java.util

class KafkaProcessingSpec extends AnyFlatSpec with Matchers {

  private val spark: SparkSession = SparkSession.builder()
    .appName("KafkaProcessingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint/KafkaProcessingSpec")
    .getOrCreate()

  import spark.implicits._

  "Kafka message processor" should "convert binary messages to strings" in {
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
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", TimestampType)
      ).as("data"))
      .select("data.*")

    val rows = kafkaDF.collect()
    rows.length should be(1)
    rows(0).getString(0) should be("789")
    rows(0).getDouble(1) should be(33.3)
    rows(0).getTimestamp(2).toInstant.toString should be("2023-01-03T15:45:30Z")
  }

  it should "handle empty messages" in {
    val kafkaData = Seq(
      (null, "".getBytes())
    )

    val schema = StructType(Seq(
      StructField("key", BinaryType, true),
      StructField("value", BinaryType, true)
    ))

    val rowList = new util.ArrayList[Row]()
    kafkaData.foreach { case (key, value) => rowList.add(Row(key, value)) }

    val kafkaDF = spark.createDataFrame(rowList, schema)
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", TimestampType)
      ).as("data"))
      .select("data.*")
      .na.drop()

    kafkaDF.collect().length should be(0)
  }

  it should "handle oversized messages" in {
    val kafkaData = Seq(
      (null, "x".repeat(1000000).getBytes())
    )

    val schema = StructType(Seq(
      StructField("key", BinaryType, true),
      StructField("value", BinaryType, true)
    ))

    val rowList = new util.ArrayList[Row]()
    kafkaData.foreach { case (key, value) => rowList.add(Row(key, value)) }

    val kafkaDF = spark.createDataFrame(rowList, schema)
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", TimestampType)
      ).as("data"))
      .select("data.*")
      .na.drop()

    kafkaDF.collect().length should be(0)
  }
}
