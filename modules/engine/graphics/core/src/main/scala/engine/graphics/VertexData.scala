package engine.graphics

trait VertexData {
  def vertices: Array[Float]
  def vertexCount: Int
}
object VertexData {
  object Quad extends VertexData {
    override val vertices: Array[Float] = Array[Float](
      // First triangle
      -0.5, 0.5, 0.0f,
      -0.5, -0.5, 0.0f,
      0.5, -0.5, 0.0f,
      // Second triangle
      -0.5, 0.5, 0.0f,
      0.5, -0.5, 0.0f,
      0.5, 0.5, 0.0f
    )
    override val vertexCount: Int = 6
  }
}
