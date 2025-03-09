import json
import time
import threading
import logging
import os
from kafka import KafkaConsumer
from flask import Flask, jsonify

app = Flask(__name__)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global variables
data_count = 0
value_sum = 0.0
last_update = None

def consume_data():
    global data_count, value_sum, last_update
    try:
        consumer = KafkaConsumer(
            os.getenv('KAFKA_TOPIC', 'raw_input'),
            bootstrap_servers=os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'kafka:9092').split(','),
            auto_offset_reset='latest'  # no value_deserializer
        )
        logger.info("Aggregator: Starting to consume data from Kafka...")

        for message in consumer:
            try:
                raw_bytes = message.value  # This is raw bytes
                data = json.loads(raw_bytes.decode('utf-8'))  # Manual JSON decode

                data_count += 1
                value_sum += data.get('value', 0)
                last_update = time.time()
                logger.info(f"Aggregator: Received data {data}")

            except json.JSONDecodeError as e:
                logger.error(f"Aggregator: Error decoding JSON: {e}")
            except Exception as e:
                logger.error(f"Aggregator: Error processing message: {e}")

    except Exception as e:
        logger.error(f"Aggregator: Error connecting to Kafka: {e}")

@app.route('/api/aggregated', methods=['GET'])
def get_aggregated():
    global data_count, value_sum, last_update
    avg = value_sum / data_count if data_count else 0
    result = {
        'count': data_count,
        'sum': value_sum,
        'average': avg,
        'last_update': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(last_update)) if last_update else None
    }
    return jsonify(result)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"}), 200

if __name__ == '__main__':
    t = threading.Thread(target=consume_data, daemon=True)
    t.start()
    logger.info("Aggregator: Starting Flask server on port 5000...")
    app.run(host='0.0.0.0', port=5000)
