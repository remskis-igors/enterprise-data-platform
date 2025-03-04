FROM openjdk:17-jdk-slim

# Install dependencies needed for sbt installation
RUN apt-get update && apt-get install -y curl tar && rm -rf /var/lib/apt/lists/*

# Install sbt manually (adjust version as needed)
RUN curl -L https://github.com/sbt/sbt/releases/download/v1.8.2/sbt-1.8.2.tgz -o /tmp/sbt.tgz && \
    tar xzf /tmp/sbt.tgz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

# Set Hadoop environment variables to satisfy Spark's checks
ENV HADOOP_HOME=/opt/hadoop
ENV hadoop.home.dir=/opt/hadoop
RUN mkdir -p /opt/hadoop

# Set working directory and copy application files
WORKDIR /app
COPY build.sbt ./
COPY project/ project/
COPY src/ src/

# Update dependencies and build the fat JAR using sbt
RUN sbt -v update
RUN sbt assembly

# Run the built JAR (adjust the path to your assembled JAR as needed)
CMD ["java", "--add-opens=java.base/java.lang=ALL-UNNAMED", "-jar", "target/scala-2.13/my-app-assembly-0.1.0.jar"]
