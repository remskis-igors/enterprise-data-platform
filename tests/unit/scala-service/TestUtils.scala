package com.example.service

import org.apache.spark.sql.types._

object TestUtils {
  // Common schema used across tests
  val messageSchema = new StructType()
    .add("id", StringType)
    .add("value", DoubleType)
    .add("event_time", StringType)

  // Helper method to create test data
  def createTestMessage(id: String, value: Double, eventTime: String): String = {
    s"""{"id": "$id", "value": $value, "event_time": "$eventTime"}"""
  }

  // Helper method to create invalid test data
  def createInvalidMessage(messageType: String): String = messageType match {
    case "malformed" => """{"id": "test" "value": 100}"""
    case "invalid_types" => """{"id": 123, "value": "not_a_number", "event_time": true}"""
    case "missing_fields" => """{"id": "test"}"""
    case _ => throw new IllegalArgumentException(s"Unknown message type: $messageType")
  }
}
