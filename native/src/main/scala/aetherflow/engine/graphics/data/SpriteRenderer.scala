package aetherflow.engine.graphics.data

import scalanative.unsafe.*
import scalanative.unsigned.*
import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.nativelink.stb_image.*

class SpriteRenderer private(
  private val VAO: GLuint,
  private val shader: Shader,
){
  def drawSprite(
    texture: Texture2D,
//    position: Vec2f
  ): Unit = Zone.acquire { implicit z =>
    shader.use()
    shader.setUniform3f(c"spriteColor", 1f, 1f, 1f)
    texture.bind()
    glBindVertexArray(VAO)
    glDrawArrays(GL_TRIANGLES, 0, 6.toUInt)
    glBindVertexArray(0.toUInt)
  }
}
object SpriteRenderer {
//  private val vertices = Array[GLfloat](
//    // pos      // tex
//    0.0f, 1.0f, 0.0f, 1.0f,
//    1.0f, 0.0f, 1.0f, 0.0f,
//    0.0f, 0.0f, 0.0f, 0.0f,
//
//    0.0f, 1.0f, 0.0f, 1.0f,
//    1.0f, 1.0f, 1.0f, 1.0f,
//    1.0f, 0.0f, 1.0f, 0.0f,
//  )
  private val vertices = Array[GLfloat](
    // pos        // tex
    -0.5f,  0.5f, 0.0f, 1.0f,
     0.5f, -0.5f, 1.0f, 0.0f,
    -0.5f, -0.5f, 0.0f, 0.0f,

    -0.5f,  0.5f, 0.0f, 1.0f,
     0.5f,  0.5f, 1.0f, 1.0f,
     0.5f, -0.5f, 1.0f, 0.0f,
  )

  def create(shader: Shader): SpriteRenderer = {
    val VBO = stackalloc[GLuint]()
    val VAO = stackalloc[GLuint]()

    glGenBuffers(1.toUInt, VBO)
    glGenVertexArrays(1.toUInt, VAO)

    glBindBuffer(GL_ARRAY_BUFFER, !VBO)
    val verticesPtr = stackalloc[GLfloat](vertices.length)
    for (i <- vertices.indices) verticesPtr(i) = vertices(i)
    glBufferData(GL_ARRAY_BUFFER, vertices.length * 4, verticesPtr.asInstanceOf[Ptr[Byte]], GL_STATIC_DRAW)

    glBindVertexArray(!VAO)
    glEnableVertexAttribArray(0.toUInt)
    glVertexAttribPointer(
      0.toUInt, 4, GL_FLOAT, GL_FALSE,
      stride = (4 * 4).toUInt,
      pointer = 0L.toPtr[Byte]
    )

    glBindBuffer(GL_ARRAY_BUFFER, 0.toUInt)
    glBindVertexArray(0.toUInt)

    new SpriteRenderer(!VAO, shader)
  }
}
