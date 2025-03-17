package com.example.service

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JsonParsingSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val spark: SparkSession = SparkSession.builder()
    .appName("JsonParsingTest")
    .master("local[2]")
    .getOrCreate()

  import spark.implicits._

  override def afterAll(): Unit = {
    spark.stop()
    super.afterAll()
  }

  // Test schema definition
  private val jsonSchema = new StructType()
    .add("id", StringType)
    .add("value", DoubleType)
    .add("event_time", StringType)
    .add("optional_field", StringType, nullable = true)

  "JSON Parser" should "handle all valid data type combinations" in {
    val validJsonData = Seq(
      """{"id": "123", "value": 42.5, "event_time": "2023-01-01T10:15:30Z", "optional_field": "test"}""",
      """{"id": "456", "value": 0.0, "event_time": "2023-01-02T12:00:00Z"}"""
    )

    val df = spark.createDataset(validJsonData).toDF("json_str")
    val parsedDF = df.select(from_json(col("json_str"), jsonSchema).as("data"))
      .select("data.*")

    val rows = parsedDF.collect()
    rows.length should be(2)
    rows(0).getString(0) should be("123")
    rows(0).getDouble(1) should be(42.5)
    rows(0).getString(3) should be("test")
    rows(1).getString(3) should be(null)
  }

  it should "handle missing required fields" in {
    val invalidJsonData = Seq(
      """{"id": "123"}""",
      """{"value": 42.5, "event_time": "2023-01-01T10:15:30Z"}"""
    )

    val df = spark.createDataset(invalidJsonData).toDF("json_str")
    val parsedDF = df.select(from_json(col("json_str"), jsonSchema).as("data"))
      .select("data.*")
      .na.drop("all") // Drop rows where all fields are null

    val rows = parsedDF.collect()
    rows.length should be(0) // All rows should be invalid
  }

  it should "handle invalid data types" in {
    val invalidTypeData = Seq(
      """{"id": 123, "value": "not-a-number", "event_time": "2023-01-01T10:15:30Z"}""",
      """{"id": "456", "value": true, "event_time": 20230102}"""
    )

    val df = spark.createDataset(invalidTypeData).toDF("json_str")
    val parsedDF = df.select(from_json(col("json_str"), jsonSchema).as("data"))
      .select("data.*")
      .na.drop("all")

    val rows = parsedDF.collect()
    rows.length should be(0) // All rows should be invalid due to type mismatch
  }

  it should "handle malformed JSON strings" in {
    val malformedData = Seq(
      """{"id": "123" "value": 42.5}""", // Missing comma
      """{"id": "456", value: 99.9}""",  // Missing quotes
      "{malformed}"
    )

    val df = spark.createDataset(malformedData).toDF("json_str")
    val parsedDF = df.select(from_json(col("json_str"), jsonSchema).as("data"))
      .select("data.*")
      .na.drop("all")

    val rows = parsedDF.collect()
    rows.length should be(0) // All rows should be invalid
  }

  it should "handle empty and null values" in {
    val emptyData = Seq(
      """{"id": "", "value": null, "event_time": null}""",
      null,
      ""
    )

    val df = spark.createDataset(emptyData).toDF("json_str")
    val parsedDF = df.select(from_json(col("json_str"), jsonSchema).as("data"))
      .select("data.*")
      .na.drop("all")

    val rows = parsedDF.collect()
    rows.length should be(0) // All rows should be invalid or empty
  }
}
