# etl_service/kafka_utils.py
from pyspark.sql import SparkSession

def create_kafka_df(spark: SparkSession, bootstrap_servers: str, topic: str):
    """
    Create DataFrame for stream reading from Kafka.
    """
    return (spark.readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", bootstrap_servers)
            .option("subscribe", topic)
            .option("startingOffsets", "latest")
            .load()
            )