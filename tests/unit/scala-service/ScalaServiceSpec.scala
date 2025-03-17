import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util

/**
 * Unit tests for Spark Streaming transformations.
 * This spec tests JSON parsing, Kafka message processing, and full pipeline execution.
 */
class ScalaServiceSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  // Create a local SparkSession for testing
  private val spark: SparkSession = SparkSession.builder()
    .appName("SparkStreamingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint")
    .getOrCreate()

  import spark.implicits._

  override def afterAll(): Unit = {
    spark.stop()
    super.afterAll()
  }

  /** Test for JSON message parsing */
  "JSON message parsing" should "correctly extract fields from valid JSON" in {
    // Define sample JSON data
    val jsonData = Seq(
      """{"id": "123", "value": 42.5, "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": 99.9, "event_time": "2023-01-02T12:00:00Z"}"""
    )

    // Create a DataFrame with the sample data
    val inputDF = spark.createDataset(jsonData).toDF("json_str")

    // Apply JSON transformation
    val parsedDF = inputDF
      .select(from_json(col("json_str"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", StringType)
      ).as("data"))
      .select("data.*")

    // Expected schema
    val expectedSchema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", StringType)

    parsedDF.schema should be(expectedSchema)

    // Validate data
    val rows = parsedDF.collect()
    rows.length should be(2)

    rows(0).getString(0) should be("123")
    rows(0).getDouble(1) should be(42.5)
    rows(0).getString(2) should be("2023-01-01T10:15:30Z")

    rows(1).getString(0) should be("456")
    rows(1).getDouble(1) should be(99.9)
    rows(1).getString(2) should be("2023-01-02T12:00:00Z")
  }

  /** Test handling of malformed JSON */
  it should "handle malformed JSON gracefully" in {
    val jsonData = Seq(
      """{"id": "123", "value": 42.5, "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": "invalid", "event_time": "2023-01-02T12:00:00Z"}""",
      """{malformed json}"""
    )

    val inputDF = spark.createDataset(jsonData).toDF("json_str")

    val parsedDF = inputDF
      .select(from_json(col("json_str"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", StringType)
      ).as("data"))
      .select("data.*")
      .na.drop() // Drop invalid rows

    val rows = parsedDF.collect()
    rows.length should be(1)

    rows(0).getString(0) should be("123")
    rows(0).getDouble(1) should be(42.5)
    rows(0).getString(2) should be("2023-01-01T10:15:30Z")
  }

  /** Test for Kafka message processing */
  "Kafka message processing" should "convert messages to the correct format" in {
    // Define mock Kafka data
    val kafkaData = Seq(
      (null, """{"id": "789", "value": 33.3, "event_time": "2023-01-03T15:45:30Z"}""".getBytes())
    )

    val schema = StructType(Seq(
      StructField("key", BinaryType, true),
      StructField("value", BinaryType, true)
    ))

    // Convert data to DataFrame using java.util.List
    val rowList = new util.ArrayList[Row]()
    kafkaData.foreach { case (key, value) =>
      rowList.add(Row(key, value))
    }

    val kafkaDF = spark.createDataFrame(rowList, schema)

    val messages = kafkaDF
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), new StructType()
        .add("id", StringType)
        .add("value", DoubleType)
        .add("event_time", StringType)
      ).as("data"))
      .select("data.*")

    val rows = messages.collect()
    rows.length should be(1)

    rows(0).getString(0) should be("789")
    rows(0).getDouble(1) should be(33.3)
    rows(0).getString(2) should be("2023-01-03T15:45:30Z")
  }

  /** Test for end-to-end pipeline processing */
  "The full pipeline" should "process messages end-to-end" in {
    // Define mock Kafka data
    val testData = Seq(
      """{"id": "abc", "value": 123.45, "event_time": "2023-01-05T08:30:00Z"}""",
      """{"id": "def", "value": 67.89, "event_time": "2023-01-05T09:00:00Z"}"""
    )

    // Create a streaming DataFrame using MemoryStream
    import org.apache.spark.sql.execution.streaming.MemoryStream
    implicit val sqlContext = spark.sqlContext
    val stream = MemoryStream[String]
    stream.addData(testData)

    // Define the schema for the JSON data
    val jsonSchema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", StringType)

    // Parse the JSON data
    val messages = stream.toDF()
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), jsonSchema).as("data"))
      .select("data.*")

    // Debugging: Print the schema and data
    println("Schema of messages DataFrame:")
    messages.printSchema()

    println("Data in messages DataFrame:")
    messages.show(false)

    // Start the streaming query
    val query = messages.writeStream
      .format("memory")
      .queryName("results")
      .outputMode(OutputMode.Append())
      .start()

    // Process all available data
    query.processAllAvailable()

    // Validate the results
    val results = spark.table("results").collect()
    results.length should be(2)

    results(0).getString(0) should be("abc")
    results(0).getDouble(1) should be(123.45)
    results(0).getString(2) should be("2023-01-05T08:30:00Z")

    results(1).getString(0) should be("def")
    results(1).getDouble(1) should be(67.89)
    results(1).getString(2) should be("2023-01-05T09:00:00Z")

    // Stop the query
    query.stop()
  }
}