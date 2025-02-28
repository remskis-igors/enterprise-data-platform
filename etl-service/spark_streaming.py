from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, TimestampType
import kafka_utils

# Creating Spark session
spark = SparkSession.builder \
    .appName("EnterpriseETL") \
    .config("spark.jars.packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0") \
    .getOrCreate()
# Incoming data scheme
schema = StructType([
    StructField("id", StringType(), True),
    StructField("value", DoubleType(), True),
    StructField("event_time", TimestampType(), True)
])

# Reading data from Kafka
df = kafka_utils.create_kafka_df(spark, "kafka:9092", "raw_input")


# JSON parsing from Kafka
parsed_df = df.selectExpr("CAST(value AS STRING) as json_value") \
    .select(from_json(col("json_value"), schema).alias("data")) \
    .select("data.*")

# at this point just output to console
query = parsed_df.writeStream \
    .outputMode("append") \
    .format("console") \
    .start()

query.awaitTermination()
