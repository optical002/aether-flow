package engine.graphics.opengl.data

import java.nio.FloatBuffer

case class MvpMatrix(
  location: UniformLocation,
  buffer: FloatBuffer
)
