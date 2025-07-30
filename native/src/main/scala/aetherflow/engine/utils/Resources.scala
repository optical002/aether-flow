package aetherflow.engine.utils

import scala.io.Source
import java.nio.file.{Files, Paths}

object Resources {
  def readText(path: String): String = {
    val filePath = Paths.get("native/src/main/resources", path)
    if (!Files.exists(filePath)) {
      throw new RuntimeException(s"Resource $path not found")
    }
    val source = Source.fromFile(filePath.toFile)
    try source.mkString finally source.close()
  }
}
