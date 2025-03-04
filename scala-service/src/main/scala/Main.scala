import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Main extends App {
  // Init SparkSession
  val spark = SparkSession.builder()
    .appName("Scala Spark Streaming Job")
    .master("local[*]")  // could be the cluster
    .getOrCreate()

  spark.sparkContext.setLogLevel("WARN")

  // Stream reading from Kafka
  val kafkaDF = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "kafka:9092")
    .option("subscribe", "topic")  // name of Kafka topic
    .load()

  // Convert byte field value to string and parse JSON
  val messages = kafkaDF.selectExpr("CAST(value AS STRING) as json_str")
    .select(from_json(col("json_str"),
      // scheme
      schema_of_json("""{"id": "string", "value": "double", "event_time": "string"}""")
    ).as("data"))
    .select("data.*")

  // Output to console (for debugging only; in production it is better to write to external storage)
  val query = messages.writeStream
    .format("console")
    .option("truncate", "false")
    .start()

  query.awaitTermination()
}
