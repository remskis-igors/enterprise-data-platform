from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, TimestampType
from etl_service.kafka_utils import create_kafka_df

def get_schema():
    """
    Returns the schema for the Kafka input data
    """
    return StructType([
        StructField("id", StringType(), True),
        StructField("value", DoubleType(), True),
        StructField("event_time", TimestampType(), True)
    ])

def run_streaming_job():
    """
    Runs the Spark streaming job for processing Kafka input
    """
    spark = (
        SparkSession.builder
        .appName("EnterpriseETL")
        .config("spark.jars.packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0")
        .getOrCreate()
    )

    # Get the schema
    schema = get_schema()

    # Create Kafka DataFrame
    df = create_kafka_df(spark, "kafka:9092", "raw_input")

    # Parse the JSON data
    parsed_df = (df
                 .selectExpr("CAST(value AS STRING) as json_value")
                 .select(from_json(col("json_value"), schema).alias("data"))
                 .select("data.*"))

    # Start the stream
    query = (
        parsed_df.writeStream
        .outputMode("append")
        .format("console")
        .start()
    )

    query.awaitTermination()