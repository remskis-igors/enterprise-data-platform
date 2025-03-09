import unittest
from unittest.mock import patch, MagicMock
import time
import json

from aggregator.aggregator import app, consume_data
import aggregator.aggregator as aggregator

class TestAggregator(unittest.TestCase):

    def setUp(self):
        # Reset global variables before each test
        aggregator.data_count = 0
        aggregator.value_sum = 0.0
        aggregator.last_update = None

        self.app = app.test_client()
        app.testing = True

    # ------------------- /api/aggregated with data -------------------
    @patch.multiple(
        'aggregator.aggregator',
        data_count=10,
        value_sum=250.0,
        last_update=time.time()  # just some non-None float
    )
    def test_get_aggregated_with_data(self):
        """
        Test /api/aggregated endpoint when aggregator has data.
        """
        response = self.app.get('/api/aggregated')
        self.assertEqual(response.status_code, 200)

        data = response.get_json()
        self.assertEqual(data['count'], 10)
        self.assertEqual(data['sum'], 250.0)
        self.assertEqual(data['average'], 25.0)
        self.assertIsNotNone(data['last_update'])

    # ------------------- /api/aggregated without data ----------------
    @patch.multiple(
        'aggregator.aggregator',
        data_count=0,
        value_sum=0.0,
        last_update=None
    )
    def test_get_aggregated_without_data(self):
        """
        Test /api/aggregated endpoint when aggregator has no data.
        """
        response = self.app.get('/api/aggregated')
        self.assertEqual(response.status_code, 200)

        data = response.get_json()
        self.assertEqual(data['count'], 0)
        self.assertEqual(data['sum'], 0.0)
        self.assertEqual(data['average'], 0)
        self.assertIsNone(data['last_update'])

    # ------------------- /health endpoint ----------------------------
    def test_health_endpoint(self):
        """
        Test /health endpoint.
        """
        response = self.app.get('/health')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.get_json(), {'status': 'healthy'})

    # ------------------- consume_data successful ----------------------
    @patch('aggregator.aggregator.KafkaConsumer')
    def test_consume_data_successful(self, mock_kafka_consumer):
        """
        Provide valid JSON => aggregator increments data_count.
        """
        mock_message = MagicMock()
        mock_message.value = b'{"value": 50}'

        mock_consumer_instance = mock_kafka_consumer.return_value
        mock_consumer_instance.__iter__.return_value = [mock_message]

        consume_data()

        self.assertEqual(aggregator.data_count, 1)
        self.assertEqual(aggregator.value_sum, 50.0)
        self.assertIsNotNone(aggregator.last_update)

    # ------------------- consume_data JSON decode error --------------
    @patch('aggregator.aggregator.KafkaConsumer')
    def test_consume_data_json_decode_error(self, mock_kafka_consumer):
        """
        Provide malformed JSON => aggregator logs 'Error decoding JSON'.
        """
        mock_message = MagicMock()
        mock_message.value = b'{"value": 50'  # missing closing brace

        mock_consumer_instance = mock_kafka_consumer.return_value
        mock_consumer_instance.__iter__.return_value = [mock_message]

        with self.assertLogs('aggregator.aggregator', level='ERROR') as log:
            consume_data()

        self.assertTrue(
            any('Error decoding JSON' in entry for entry in log.output),
            msg=f"Expected 'Error decoding JSON' in logs, got: {log.output}"
        )
        self.assertEqual(aggregator.data_count, 0)

    # ------------------- consume_data generic exception --------------
    @patch('aggregator.aggregator.KafkaConsumer')
    def test_consume_data_generic_exception(self, mock_kafka_consumer):
        """
        Force a generic Exception => aggregator logs 'Error connecting to Kafka'.
        """
        mock_consumer_instance = mock_kafka_consumer.return_value
        mock_consumer_instance.__iter__.side_effect = Exception("Generic Kafka error")

        with self.assertLogs('aggregator.aggregator', level='ERROR') as log:
            consume_data()

        self.assertTrue(
            any('Error connecting to Kafka' in message or 'Generic Kafka error' in message
                for message in log.output),
            msg=f"Expected 'Error connecting to Kafka' in logs, got: {log.output}"
        )

if __name__ == '__main__':
    unittest.main()
