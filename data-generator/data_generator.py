import json
import time
import random
from kafka import KafkaProducer

producer = KafkaProducer(
    bootstrap_servers="localhost:9092",
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

while True:
    data = {
        "id": f"user_{random.randint(1, 100)}",
        "value": random.uniform(10.0, 500.0),
        "event_time": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    }
    producer.send("raw_input", value=data)
    print(f"Sent: {data}")
    time.sleep(2)
