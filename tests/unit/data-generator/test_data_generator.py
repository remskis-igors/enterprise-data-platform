import json
import time
import random
from kafka import KafkaProducer
import unittest
from unittest.mock import MagicMock

# The code that generates data and sends it to Kafka
def generate_data_and_send(producer):
    """Generates data and sends it to Kafka."""
    data = {
        "id": f"user_{random.randint(1, 100)}",
        "value": random.uniform(10.0, 500.0),
        "event_time": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    }
    producer.send("raw_input", value=data)
    return data


class TestDataGenerator(unittest.TestCase):
    """Unit tests for the data generator component"""

    def setUp(self):
        """Mock KafkaProducer to avoid real data sending"""
        self.mock_producer = MagicMock(spec=KafkaProducer)
        self.mock_producer.send = MagicMock()  # Mock the send method

    def test_generate_data_structure(self):
        """Test that generated data has correct structure and types."""
        data = generate_data_and_send(self.mock_producer)

        # Check that all necessary fields are present
        self.assertIn("id", data)
        self.assertIn("value", data)
        self.assertIn("event_time", data)

        # Check the data types
        self.assertIsInstance(data["id"], str)
        self.assertIsInstance(data["value"], float)
        self.assertIsInstance(data["event_time"], str)

        # Check that the ID format is correct (should start with 'user_')
        self.assertTrue(data["id"].startswith("user_"))

        # Check the value field is within the correct range
        self.assertGreaterEqual(data["value"], 10.0)
        self.assertLessEqual(data["value"], 500.0)

    def test_random_data_generation(self):
        """Test that data is generated randomly."""
        data1 = generate_data_and_send(self.mock_producer)
        data2 = generate_data_and_send(self.mock_producer)

        # Check that the generated data is different each time
        self.assertNotEqual(data1, data2)

    def test_send_data(self):
        """Test sending the generated data via Kafka (mocked)."""
        data = generate_data_and_send(self.mock_producer)

        # Check that data was sent via the mock producer
        self.mock_producer.send.assert_called_with("raw_input", value=data)

        # Verify that the sent data matches the generated data
        self.assertEqual(data, self.mock_producer.send.call_args[1]['value'])


if __name__ == "__main__":
    unittest.main()
