import json
import time
import threading
from kafka import KafkaConsumer
from flask import Flask, jsonify

app = Flask(__name__)

# Global variables for aggregated data
data_count = 0
value_sum = 0.0
last_update = None

def consume_data():
    global data_count, value_sum, last_update
    # Create a KafkaConsumer that reads from the 'raw_input' topic
    consumer = KafkaConsumer(
        'raw_input',
        bootstrap_servers=['kafka:9092'],  # Kafka addr
        auto_offset_reset='latest',
        value_deserializer=lambda m: json.loads(m.decode('utf-8'))
    )
    print("Aggregator: Starting to consume data from Kafka...")
    for message in consumer:
        data = message.value
        data_count += 1
        # Assums, that every message has "value" field
        value_sum += data.get('value', 0)
        last_update = time.time()
        print(f"Aggregator: Received data {data}")

@app.route('/api/aggregated', methods=['GET'])
def get_aggregated():
    global data_count, value_sum, last_update
    avg = value_sum / data_count if data_count > 0 else 0
    # Forming an aggregated result
    result = {
        'count': data_count,
        'sum': value_sum,
        'average': avg,
        'last_update': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(last_update)) if last_update else None
    }
    return jsonify(result)

if __name__ == '__main__':
    # Run Kafka consumer in a separate thread
    consumer_thread = threading.Thread(target=consume_data, daemon=True)
    consumer_thread.start()

    # Launch Flask server on port 5000
    print("Aggregator: Starting Flask server on port 5000...")
    app.run(host='0.0.0.0', port=5000)
