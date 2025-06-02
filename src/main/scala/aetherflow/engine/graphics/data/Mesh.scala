package aetherflow.engine.graphics.data

import org.joml.*
import org.lwjgl.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL14.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.assimp.*

class Mesh private(
  val vertices: Array[Vertex],
  val indices: Array[Short],
//  val textures: Array[Texture],
  val material: Option[Material],
  private val VAO: Int,
  private val VBO: Int,
  private val EBO: Int,
) {
  def draw(shader: Shader): Unit = {
//    val values = textures.groupBy(_.kind).toSeq.flatMap { case (kind, textures) =>
//      val name = kind match {
//        case Texture.Kind.Diffuse => "texture_diffuse"
//        case Texture.Kind.Specular => "texture_specular"
//      }
//      textures.zipWithIndex.map { case (texture, i) => (texture, s"$name") }
//    }
//    for (((texture, shaderName), i) <- values.zipWithIndex) {
//      glActiveTexture(GL_TEXTURE0 + i)
//      println(s"texture $i: $shaderName,,, ${texture.id},,, $VAO")
//      glBindTexture(GL_TEXTURE_2D, texture.id)
//      shader.setFloat(shaderName, i)
//    }
    material match {
      case Some(value) => value.apply(shader)
      case None => ()
    }

    // Draw mesh
    glBindVertexArray(VAO)
    glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, 0)

    // Reset
    glBindVertexArray(0)
    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, 0)
    shader.setInt("texture_diffuse", 0)
    shader.setInt("texture_specular", 0)
  }
}
object Mesh {
  /** Note: Creation should happen inside valid OpenGL context. */
  def create(
    vertices: Array[Vertex],
    indices: Array[Short],
    material: Option[Material],
  ): Mesh = {
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
    val ebo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    val verticesFloatArray = vertices.asFloatArray
    val vboBuffer = BufferUtils.createFloatBuffer(verticesFloatArray.length)
    vboBuffer.put(verticesFloatArray).flip()
    glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    val eboBuffer = BufferUtils.createShortBuffer(indices.length)
    eboBuffer.put(indices).flip()
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, eboBuffer, GL_STATIC_DRAW)

    // vertex positions
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.sizeOf, Vertex.positionOffset)

    // vertex normals
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.sizeOf, Vertex.normalOffset)

    // vertex texture coords
    glEnableVertexAttribArray(2)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, Vertex.sizeOf, Vertex.texCoordsOffset)

    glBindVertexArray(0)

    new Mesh(
      vertices, indices, material, vao, vbo, ebo
    )
  }
}
