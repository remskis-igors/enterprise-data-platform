package com.example.service

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.util

class KafkaProcessingSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val spark: SparkSession = SparkSession.builder()
    .appName("KafkaProcessingTest")
    .master("local[2]")
    .getOrCreate()

  import spark.implicits._

  override def afterAll(): Unit = {
    spark.stop()
    super.afterAll()
  }

  private val kafkaSchema = StructType(Seq(
    StructField("key", BinaryType, true),
    StructField("value", BinaryType, true),
    StructField("topic", StringType, true),
    StructField("partition", IntegerType, true),
    StructField("offset", LongType, true),
    StructField("timestamp", TimestampType, true)
  ))

  "Kafka message processor" should "convert binary messages to strings" in {
    val message = """{"id": "789", "value": 33.3, "event_time": "2023-01-03T15:45:30Z"}"""
    val kafkaData = Seq(
      (null, message.getBytes(), "test-topic", 0, 0L, java.sql.Timestamp.valueOf("2023-01-03 15:45:30"))
    )

    val rowList = new util.ArrayList[org.apache.spark.sql.Row]()
    kafkaData.foreach { case (key, value, topic, partition, offset, timestamp) =>
      rowList.add(org.apache.spark.sql.Row(key, value, topic, partition, offset, timestamp))
    }

    val kafkaDF = spark.createDataFrame(rowList, kafkaSchema)

    val messages = kafkaDF
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")

    val rows = messages.collect()
    rows.length should be(1)
    rows(0).getString(0) should be("789")
    rows(0).getDouble(1) should be(33.3)
  }

  it should "handle empty messages" in {
    val kafkaData = Seq(
      (null, Array[Byte](), "test-topic", 0, 0L, java.sql.Timestamp.valueOf("2023-01-03 15:45:30")),
      (null, null, "test-topic", 0, 1L, java.sql.Timestamp.valueOf("2023-01-03 15:45:31"))
    )

    val rowList = new util.ArrayList[org.apache.spark.sql.Row]()
    kafkaData.foreach { case (key, value, topic, partition, offset, timestamp) =>
      rowList.add(org.apache.spark.sql.Row(key, value, topic, partition, offset, timestamp))
    }

    val kafkaDF = spark.createDataFrame(rowList, kafkaSchema)

    val messages = kafkaDF
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")
      .na.drop("all")

    val rows = messages.collect()
    rows.length should be(0) // All messages should be filtered out
  }

  it should "handle oversized messages" in {
    // Create a large message that exceeds typical Kafka message size
    val largeValue = "x" * 2000000 // 2MB string
    val kafkaData = Seq(
      (null, largeValue.getBytes(), "test-topic", 0, 0L, java.sql.Timestamp.valueOf("2023-01-03 15:45:30"))
    )

    val rowList = new util.ArrayList[org.apache.spark.sql.Row]()
    kafkaData.foreach { case (key, value, topic, partition, offset, timestamp) =>
      rowList.add(org.apache.spark.sql.Row(key, value, topic, partition, offset, timestamp))
    }

    val kafkaDF = spark.createDataFrame(rowList, kafkaSchema)

    val messages = kafkaDF
      .withColumn("value", col("value").cast("string"))
      .select(from_json(col("value"), TestUtils.messageSchema).as("data"))
      .select("data.*")
      .na.drop("all")

    val rows = messages.collect()
    rows.length should be(0) // Message should be too large to process
  }
}
