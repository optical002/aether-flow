package aetherflow.engine.utils

import scala.io.Source

object Resources {
  def getPath(path: String): String = {
    Option(getClass.getResource(s"/$path"))
      .map(_.getPath)
      .getOrElse(throw new RuntimeException(s"Resource $path not found"))
  }
  
  def readText(path: String): String = {
    val stream = Option(getClass.getResourceAsStream(s"/$path"))
      .getOrElse(throw new RuntimeException(s"Resource $path not found"))
    Source.fromInputStream(stream, "UTF-8").mkString
  }
}
