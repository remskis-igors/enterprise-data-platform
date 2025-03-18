import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.types._
import org.apache.spark.sql.SQLContext

object TestUtils {

  def createSparkSession(appName: String, checkpointDir: String): SparkSession = {
    val spark = SparkSession.builder()
      .appName(appName)
      .master("local[2]")
      .config("spark.sql.streaming.checkpointLocation", checkpointDir)
      .getOrCreate()

    val checkpointPath = new java.io.File(checkpointDir)
    if (checkpointPath.exists()) {
      checkpointPath.delete()
    }

    spark
  }

  def createMemoryStream(spark: SparkSession, schema: StructType): MemoryStream[String] = {
    import spark.implicits._
    implicit val sqlContext: SQLContext = spark.sqlContext
    MemoryStream[String]
  }
}
