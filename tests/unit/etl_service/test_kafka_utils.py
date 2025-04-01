import pytest
from unittest.mock import MagicMock, patch
from pyspark.sql import SparkSession
from etl_service.kafka_utils import create_kafka_df

def test_create_kafka_df():
    """
    A simple unit test that checks if create_kafka_df
    calls methods in the correct order:
      - format("kafka")
      - option("subscribe", ...)
      - option("startingOffsets", ...)
      - load()
    and returns the correct mock object.
    """
    # Create the main mock for SparkSession
    spark_mock = MagicMock(spec=SparkSession)

    # Mock readStream.format(...) to return our mock
    read_stream_mock = MagicMock()
    spark_mock.readStream.format.return_value = read_stream_mock

    # Make each .option(...) call return the same object
    # by using a "self-returning" pattern:
    read_stream_mock.option.return_value = read_stream_mock

    # load_mock will be the object returned when .load() is called
    load_mock = MagicMock()
    read_stream_mock.load.return_value = load_mock

    # Call the function under test
    result_df = create_kafka_df(spark_mock, "kafka:9092", "test_topic")

    # Check that the format was set to "kafka"
    spark_mock.readStream.format.assert_called_once_with("kafka")

    # Check that the "subscribe" and "startingOffsets" options were set
    read_stream_mock.option.assert_has_calls([
        (("subscribe", "test_topic"),),
        (("startingOffsets", "latest"),),
    ], any_order=False)

    # Remove/add this part if you want to check for bootstrap.servers:
    # read_stream_mock.option.assert_any_call("kafka.bootstrap.servers", "kafka:9092")

    # Verify that the function result is the same object returned by load()
    assert result_df == load_mock


def test_create_kafka_df_is_streaming():
    """
    Additionally, verify that the returned DataFrame is marked as streaming.
    For this, we mock the .isStreaming property on load_mock.
    """
    spark_mock = MagicMock(spec=SparkSession)
    read_stream_mock = MagicMock()
    spark_mock.readStream.format.return_value = read_stream_mock
    read_stream_mock.option.return_value = read_stream_mock

    load_mock = MagicMock()
    # Imitate that it really is a streaming DF:
    load_mock.isStreaming = True
    read_stream_mock.load.return_value = load_mock

    result_df = create_kafka_df(spark_mock, "kafka:9092", "test_topic")

    assert result_df.isStreaming is True, "Expected the DataFrame to be streaming"
