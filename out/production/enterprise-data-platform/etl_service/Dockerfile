FROM python:3.9-slim

# Java installation (requiered for Spark)
RUN apt-get update && apt-get install -y openjdk-17-jdk-headless && rm -rf /var/lib/apt/lists/*


WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

CMD ["python", "spark_streaming.py"]
