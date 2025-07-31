package aetherflow.engine.utils

import scala.io.Source
import java.nio.file.{Files, Path, Paths}

object Resources {
  def getPath(path: String): Path = {
    val filePath = Paths.get("native/src/main/resources", path)
    if (!Files.exists(filePath)) {
      throw new RuntimeException(s"Resource ${filePath.toString} not found")
    }
    filePath
  }
  
  def readText(path: String): String = {
    val source = Source.fromFile(getPath(path).toFile)
    try source.mkString finally source.close()
  }
}
