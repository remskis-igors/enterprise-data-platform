// Define the name of your project
name := "scala-service"

// Define the version of your project
version := "0.1.0"

// Define the Scala version to use
scalaVersion := "2.13.10"

// Add common settings
lazy val commonSettings = Seq(
  organization := "com.example",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings"),
  javacOptions ++= Seq("-source", "11", "-target", "11"),
  // Export internal package to allow Spark to access sun.nio.ch.DirectBuffer
  javaOptions ++= Seq("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED")
)

// Define the main project
lazy val root = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      // Spark SQL dependency
      "org.apache.spark" %% "spark-sql" % "3.3.0",
      // Kafka dependency (adjust if needed)
      "org.apache.kafka" %% "kafka" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.10"
    ),
    resolvers ++= Seq(
      "Apache Releases" at "https://repository.apache.org/content/repositories/releases/",
      "Maven Central" at "https://repo1.maven.org/maven2/"
    )
  )

// Assembly plugin settings for creating a fat JAR
assembly / assemblyJarName := "my-app-assembly-0.1.0.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// Test configuration
Test / fork := true
Test / javaOptions ++= Seq(
  "-Xms512M",
  "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)
Test / parallelExecution := false
