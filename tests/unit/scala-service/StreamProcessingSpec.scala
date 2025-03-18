import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.OutputMode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File

class StreamProcessingSpec extends AnyFlatSpec with Matchers {

  private val spark: SparkSession = SparkSession.builder()
    .appName("StreamProcessingTest")
    .master("local[2]")
    .config("spark.sql.streaming.checkpointLocation", "target/checkpoint/StreamProcessingSpec")
    .getOrCreate()

  import spark.implicits._

  "Stream processor" should "handle basic stream operations" in {
    val testData = Seq(
      """{"id": "abc", "value": 123.45, "event_time": "2023-01-05T08:30:00Z"}""",
      """{"id": "def", "value": 67.89, "event_time": "2023-01-05T09:00:00Z"}"""
    )

    import org.apache.spark.sql.execution.streaming.MemoryStream
    implicit val sqlContext = spark.sqlContext
    val stream = MemoryStream[String]
    stream.addData(testData)

    val schema = new StructType()
      .add("id", StringType)
      .add("value", DoubleType)
      .add("event_time", TimestampType)

    val messages = stream.toDF()
      .select(from_json(col("value"), schema).as("data"))
      .select("data.*")

    val query = messages.writeStream
      .format("memory")
      .queryName("stream_results")
      .outputMode(OutputMode.Append())
      .option("checkpointLocation", "target/checkpoint/StreamProcessing")
      .start()

    query.processAllAvailable()

    val results = spark.table("stream_results").collect()
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
