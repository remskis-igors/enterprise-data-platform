import pytest
from unittest.mock import MagicMock, patch
from etl_service.spark_streaming import run_streaming_job

@patch("etl_service.spark_streaming.SparkSession")
@patch("etl_service.spark_streaming.create_kafka_df")
@patch("etl_service.spark_streaming.col")
@patch("etl_service.spark_streaming.from_json")
def test_run_streaming_job(
        from_json_mock,
        col_mock,
        create_kafka_df_mock,
        spark_mock
):
    """
    Unit test for run_streaming_job:
      - Create SparkSession through builder.appName(...).config(...).getOrCreate()
      - Call create_kafka_df(spark, <servers>, <topic>)
      - Perform transformations like selectExpr("CAST(value AS STRING) as json_value"),
        select(from_json(col("json_value"), schema).alias("data")), select("data.*")
      - Start the stream via writeStream.outputMode("append").format("console").start()
      - Call awaitTermination()
    """

    # 1. Preparing a mock for SparkSession
    builder_mock = MagicMock()
    spark_mock.builder = builder_mock

    # Configure builder mock chain
    builder_mock.appName.return_value = builder_mock
    builder_mock.config.return_value = builder_mock

    spark_session_mock = MagicMock()
    builder_mock.getOrCreate.return_value = spark_session_mock

    # 2. Mocking create_kafka_df to return df_mock
    df_mock = MagicMock()
    create_kafka_df_mock.return_value = df_mock

    # 3. Setting up the transformation chain on df_mock
    select_expr_mock = MagicMock()
    df_mock.selectExpr.return_value = select_expr_mock

    select_after_json_mock = MagicMock()
    select_expr_mock.select.return_value = select_after_json_mock

    final_select_mock = MagicMock()
    select_after_json_mock.select.return_value = final_select_mock

    # 4. Mocking writeStream chain
    write_stream_mock = MagicMock()
    final_select_mock.writeStream = write_stream_mock

    output_mode_mock = MagicMock()
    write_stream_mock.outputMode.return_value = output_mode_mock

    format_mock = MagicMock()
    output_mode_mock.format.return_value = format_mock

    start_mock = MagicMock()
    format_mock.start.return_value = start_mock

    # 5. Calling the function under test
    run_streaming_job()

    # -- Verifications --

    # A) Checking SparkSession.builder call
    builder_mock.appName.assert_called_once_with("EnterpriseETL")
    builder_mock.config.assert_called_once_with(
        "spark.jars.packages",
        "org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0"
    )
    builder_mock.getOrCreate.assert_called_once()

    # B) Checking create_kafka_df call with the correct parameters
    create_kafka_df_mock.assert_called_once_with(spark_session_mock, "kafka:9092", "raw_input")

    # C) Verifying transformations: selectExpr -> select -> select
    df_mock.selectExpr.assert_called_once_with("CAST(value AS STRING) as json_value")
    select_expr_mock.select.assert_called()
    select_after_json_mock.select.assert_called_once_with("data.*")

    # Verifying col("json_value") and from_json(...) were called
    col_mock.assert_called_once_with("json_value")
    from_json_mock.assert_called_once()

    # D) Checking the stream start
    write_stream_mock.outputMode.assert_called_once_with("append")
    output_mode_mock.format.assert_called_once_with("console")
    format_mock.start.assert_called_once()

    # E) Verifying awaitTermination call
    start_mock.awaitTermination.assert_called_once()