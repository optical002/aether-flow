package engine.graphics.opengl.shaders

import engine.graphics.Shader
import org.lwjgl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

object StandardShader extends Shader {
  object Variables {
    val aPos = "aPos"
    val uModel = "uModel"
  }
  import Variables.*

//  override def vertexShader: String =
//    s"""
//       |#version 330 core
//       |
//       |layout(location = 0) in vec3 $aPos;
//       |uniform mat4 $uModel;
//       |
//       |void main() {
//       |    gl_Position = $uModel * vec4($aPos, 1.0);
//       |}
//       |""".stripMargin

  override def vertexShader: String =
    s"""
       |#version 330 core
       |
       |layout(location = 0) in vec3 $aPos;
       |
       |void main() {
       |    gl_Position = vec4($aPos, 1.0);
       |}
       |""".stripMargin
  override def fragmentShader: String =
    """
      |#version 330 core
      |
      |out vec4 FragColor;
      |
      |void main() {
      |    FragColor = vec4(1.0, 0.6, 0.2, 1.0); // orange
      |}
      |""".stripMargin

  private var modelLoc = -1

  override def initialize(shaderProgram: Int): Unit = {
//    modelLoc = glGetUniformLocation(shaderProgram, uModel)
  }
  override def render(): Unit = {
//    val modelMatrix = transform.transformMatrix()
//    val modelBuffer = BufferUtils.createFloatBuffer(16)
//    modelMatrix.get(modelBuffer)
//
//    glUniformMatrix4fv(modelLoc, false, modelBuffer)
  }
}
