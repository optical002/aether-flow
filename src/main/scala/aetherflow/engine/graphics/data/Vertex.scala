package aetherflow.engine.graphics.data

case class Vertex(position: Vec3f, normal: Vec3f, texCoords: Vec2f) {
  lazy val asFloatArray = Array(
    position.x, position.y, position.z, normal.x, normal.y, normal.z, texCoords.x, texCoords.y
  )
}
object Vertex {
  lazy val sizeOf = Vec3f.sizeOf + Vec3f.sizeOf + Vec2f.sizeOf
  lazy val positionOffset = 0
  lazy val normalOffset = Vec3f.sizeOf
  lazy val texCoordsOffset = Vec3f.sizeOf + Vec3f.sizeOf
  
  extension (vertexArray: Array[Vertex]) {
    def asFloatArray: Array[Float] = vertexArray.flatMap(_.asFloatArray)
  }
}
