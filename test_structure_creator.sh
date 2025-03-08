#!/bin/bash

#  Folder structure creation
mkdir -p tests/{unit,integration,e2e}

# Creation of stucture of files for  unit-tests
mkdir -p tests/unit/{aggregator,data-generator,etl-service,scala-service,dashboard,go-service,rust-service,csharp-gateway,r-shiny-dashboard}

touch tests/unit/aggregator/test_aggregator.py
echo "# TODO: Implement unit tests for aggregator" > tests/unit/aggregator/test_aggregator.py

touch tests/unit/data-generator/test_data_generator.py
echo "# TODO: Implement unit tests for data-generator" > tests/unit/data-generator/test_data_generator.py

touch tests/unit/etl-service/test_etl.py
echo "# TODO: Implement unit tests for etl-service" > tests/unit/etl-service/test_etl.py

touch tests/unit/scala-service/ScalaServiceSpec.scala
echo "// TODO: Implement unit tests for scala-service" > tests/unit/scala-service/ScalaServiceSpec.scala

touch tests/unit/dashboard/dashboard.test.js
echo "// TODO: Implement unit tests for dashboard" > tests/unit/dashboard/dashboard.test.js

touch tests/unit/go-service/go_service_test.go
echo "// TODO: Implement unit tests for go-service" > tests/unit/go-service/go_service_test.go

touch tests/unit/rust-service/rust_service_tests.rs
echo "// TODO: Implement unit tests for rust-service" > tests/unit/rust-service/rust_service_tests.rs

touch tests/unit/csharp-gateway/GatewayTests.cs
echo "// TODO: Implement unit tests for csharp-gateway" > tests/unit/csharp-gateway/GatewayTests.cs

touch tests/unit/r-shiny-dashboard/test_dashboard.R
echo "# TODO: Implement unit tests for r-shiny-dashboard" > tests/unit/r-shiny-dashboard/test_dashboard.R

# Creation of files for integration-tests
touch tests/integration/kafka_integration_test.py
echo "# TODO: Implement Kafka integration tests" > tests/integration/kafka_integration_test.py

touch tests/integration/spark_integration_test.py
echo "# TODO: Implement Spark integration tests" > tests/integration/spark_integration_test.py

touch tests/integration/api_endpoints_test.py
echo "# TODO: Implement API endpoints integration tests" > tests/integration/api_endpoints_test.py

touch tests/integration/dashboard_api_test.js
echo "// TODO: Implement Dashboard API integration tests" > tests/integration/dashboard_api_test.js

#  e2e-test file creation
touch tests/e2e/system_e2e_test.py
echo "# TODO: Implement system end-to-end tests" > tests/e2e/system_e2e_test.py

echo "Test structure successfully created!"
