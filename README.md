Enterprise Data Platform

An all-in-one, polyglot data platform demonstrating real-time data ingestion, transformation, aggregation, and visualization. This platform uses Docker Compose to orchestrate a suite of services (Kafka, Spark, databases, dashboards, etc.) written in multiple programming languages.

Overview

This repository showcases a streaming architecture with the following goals:

    Data Ingestion (Python-based data-generator, Go service, Rust service, Scala Spark job)
    Real-Time Aggregation (Python aggregator)
    Orchestration (Kafka + Zookeeper + Kafka Connect + Schema Registry)
    Analytics & Visualization (Prometheus, Grafana, R Shiny Dashboard)
    Microservices in multiple languages (Go, Scala, Rust, R, C# .NET, Python)

Architecture

                +----------------+          +----------------+
                | data-generator | ---->    |     Kafka      |
                +----------------+          +----------------+
                        |                            |
                    (messages)                   (messages)
                        v                            v
        +-------------------------+           +------------------------+
        |  aggregator (Python)   |           |  etl-service (Python)  |
        +-------------------------+           +------------------------+
                | (REST API)               (Spark Streaming job in Scala)
                v
      +----------------+             +-----------------+          +----------------------+
      |  Postgres     |             |  Prometheus     |          |  R Shiny Dashboard   |
      +----------------+            +-----------------+          +----------------------+
                                              |                          ^
                                        (metrics)                        |
                                              v                          |
                                         +-----------+                   |
                                         |  Grafana  | <-----------------+
                                         +-----------+

Key Components

    Kafka & Zookeeper
    Core messaging layer.
        Confluentinc/cp-kafka
        Confluentinc/cp-zookeeper

    Schema Registry & Kafka Connect
    For schema evolution and external connectors.

    Data Generator (Python)
    Sends random or synthetic data to Kafka topics.

    ETL Service (Python, Spark Streaming)
    Consumes Kafka data, transforms it using Spark, can output to DB or other sinks.

    Aggregator (Python Flask)
    Aggregates data from Kafka or DB, exposes a simple REST endpoint.

    Scala Service
    Example microservice performing streaming or batch jobs in Scala/Spark.

    Go Service
    Example microservice that might do real-time notifications or other tasks in Go.

    Rust Service
    High-performance service for encryption/conversion or similar tasks in Rust.

    R Shiny Dashboard
    Interactive analytics dashboard built in R.

    C# Gateway
    ASP.NET Core gateway for authentication or routing.

    Prometheus & Grafana
    Monitoring stack for system metrics, custom metrics, and dashboards.

    Postgres
    Relational database used by aggregator or other services for persistent storage.

Prerequisites

    Docker and Docker Compose installed locally
    Recommended: at least 8GB RAM allocated to Docker for smoother operation
    Git (if you’re cloning this repository)

Getting Started

    Clone the repository:

git clone https://github.com/remskis-igors/enterprise-data-platform.git
cd enterprise-data-platform

Build and start all services:

docker-compose up --build

This will pull or build images for all the services and start them in the correct order.

Verify running containers:

docker-compose ps

You should see containers for Kafka, Zookeeper, aggregator, etc.

Access the dashboards:

    Grafana: http://localhost:3000 (default credentials usually admin/admin)
    Prometheus: http://localhost:9090
    R Shiny: http://localhost:3838
    Dashboard (Node/React): http://localhost:3001
    C# Gateway: http://localhost:8080
    etc.
