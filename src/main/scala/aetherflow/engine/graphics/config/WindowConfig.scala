package aetherflow.engine.graphics.config

trait WindowConfig {
  val title: String
  val width: Int
  val height: Int
  val frameRate: Int
  
  lazy val aspectRatio: Float = width.toFloat / height
}
