package com.example.service

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.functions._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.spark.sql.execution.streaming.MemoryStream
import scala.concurrent.duration._

class StreamProcessingSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val spark: SparkSession = SparkSession.builder()
    .appName("StreamProcessingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint")
    .getOrCreate()

  import spark.implicits._

  override def afterAll(): Unit = {
    spark.stop()
    super.afterAll()
  }

  "Stream processor" should "handle basic stream operations" in {
    implicit val sqlContext = spark.sqlContext
    val inputStream = MemoryStream[String]

    val query = inputStream.toDF()
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")
      .writeStream
      .format("memory")
      .queryName("test_stream")
      .outputMode(OutputMode.Append())
      .start()

    // Add test data
    inputStream.addData(
      """{"id": "test1", "value": 100.0, "event_time": "2023-01-01T10:00:00Z"}""",
      """{"id": "test2", "value": 200.0, "event_time": "2023-01-01T11:00:00Z"}"""
    )

    query.processAllAvailable()

    val results = spark.sql("SELECT * FROM test_stream").collect()
    results.length should be(2)
    results(0).getString(0) should be("test1")
    results(1).getString(0) should be("test2")

    query.stop()
  }

  it should "handle error scenarios in stream" in {
    implicit val sqlContext = spark.sqlContext
    val inputStream = MemoryStream[String]

    val query = inputStream.toDF()
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")
      .writeStream
      .format("memory")
      .queryName("error_stream")
      .outputMode(OutputMode.Append())
      .start()

    // Add invalid data
    inputStream.addData(
      """{"id": "test1", "value": "invalid", "event_time": "2023-01-01T10:00:00Z"}""",
      """invalid json""",
      """{"id": "test2", "value": 200.0, "event_time": "2023-01-01T11:00:00Z"}"""
    )

    query.processAllAvailable()

    val results = spark.sql("SELECT * FROM error_stream").collect()
    results.length should be(1) // Only valid record should be processed
    results(0).getString(0) should be("test2")

    query.stop()
  }

  it should "maintain state across batches" in {
    implicit val sqlContext = spark.sqlContext
    val inputStream = MemoryStream[String]

    // Use watermarking and window functions to test state management
    val windowedCounts = inputStream.toDF()
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")
      .withWatermark("event_time", "10 minutes")
      .groupBy(window(col("event_time"), "5 minutes"))
      .count()
      .writeStream
      .format("memory")
      .queryName("stateful_stream")
      .outputMode(OutputMode.Complete())
      .start()

    // Add data with timestamps in same window
    inputStream.addData(
      """{"id": "test1", "value": 100.0, "event_time": "2023-01-01T10:00:00Z"}""",
      """{"id": "test2", "value": 200.0, "event_time": "2023-01-01T10:02:00Z"}"""
    )

    windowedCounts.processAllAvailable()

    val results = spark.sql("SELECT * FROM stateful_stream ORDER BY window.start").collect()
    results.length should be(1) // Should have one window
    results(0).getLong(1) should be(2) // Window should contain 2 records

    windowedCounts.stop()
  }
}
