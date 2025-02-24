FROM maven:3.8.5-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/orchestration-service.jar orchestration-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "orchestration-service.jar"]
