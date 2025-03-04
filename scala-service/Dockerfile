FROM openjdk:17-jdk-slim

# Install necessary packages
RUN apt-get update && apt-get install -y curl tar

# Download and install sbt
RUN curl -L https://github.com/sbt/sbt/releases/download/v1.8.2/sbt-1.8.2.tgz -o /tmp/sbt.tgz \
  && tar xzf /tmp/sbt.tgz -C /usr/local \
  && ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

# Set the working directory
WORKDIR /app

# Copy build files
COPY build.sbt .
COPY project/ project/
COPY src/ src/

# Update dependencies and build the project
RUN sbt -v update
RUN sbt assembly

# Run the application
CMD ["java", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "-jar", "target/scala-2.13/my-app-assembly-0.1.0.jar"]