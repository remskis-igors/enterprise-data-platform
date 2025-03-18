name := "scala-service"
version := "0.1.0"
scalaVersion := "2.13.10"

lazy val commonSettings = Seq(
  organization := "com.example",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings"),
  javacOptions ++= Seq("-source", "11", "-target", "11"),
  javaOptions ++= Seq("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED")
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % "3.4.0",
      "org.apache.kafka" %% "kafka" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.10"
    ),
    resolvers ++= Seq(
      "Apache Releases" at "https://repository.apache.org/content/repositories/releases/",
      "Maven Central" at "https://repo1.maven.org/maven2/"
    ),
    Test / scalaSource := baseDirectory.value.getParentFile / "tests" / "unit" / "scala-service", // 
    Test / fork := true,
    Test / javaOptions ++= Seq(
      "-Xms512M",
      "-Xmx2048M"
    ),
    Test / parallelExecution := false
  )

assembly / assemblyJarName := "my-app-assembly-0.1.0.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
