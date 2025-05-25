package aetherflow.renderEngine

import org.joml.*
import org.lwjgl.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*


class Shader private (val programId: Int) {
  def use(): Unit = 
    glUseProgram(programId)
    
  def setBool(name: String, value: Boolean): Unit =
    glUniform1i(glGetUniformLocation(programId, name), if (value) 1 else 0)
    
  def setInt(name: String, value: Int): Unit =
    glUniform1i(glGetUniformLocation(programId, name), value)
    
  def setFloat(name: String, value: Float): Unit =
    glUniform1f(glGetUniformLocation(programId, name), value)

  def setVec4f(name: String, value: Vector4f): Unit =
    glUniform4f(glGetUniformLocation(programId, name), value.x, value.y, value.z, value.w)
    
  def setMat4f(name: String, value: Matrix4f): Unit = {
    val stack = MemoryStack.stackPush()
    val fb = stack.mallocFloat(16)
    value.get(fb)
    glUniformMatrix4fv(glGetUniformLocation(programId, name), false, fb)
    stack.close()
  }
}
object Shader {
  def create(
    vertexShaderSourcePath: String,
    fragmentShaderSourcePath: String
  ): Shader = {
    val vertexShaderSource = Resources.readText(path = vertexShaderSourcePath)
    val fragmentShaderSource = Resources.readText(path = fragmentShaderSourcePath)

    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertexShader, vertexShaderSource)
    glCompileShader(vertexShader)
    if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
      throw new RuntimeException(
        s"Vertex (path=$vertexShaderSourcePath) shader compilation failed with " +
        s"error: ${glGetShaderInfoLog(vertexShader)} "
      )
    }

    val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragmentShader, fragmentShaderSource)
    glCompileShader(fragmentShader)
    if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
      throw new RuntimeException(
        s"Vertex (path=$fragmentShaderSourcePath) shader compilation failed with " +
          s"error: ${glGetShaderInfoLog(fragmentShader)} "
      )
    }
    
    val programId = glCreateProgram()
    glAttachShader(programId, vertexShader)
    glAttachShader(programId, fragmentShader)
    glLinkProgram(programId)
    if (glGetProgrami(programId, GL_LINK_STATUS) != GL_TRUE) {
      throw new RuntimeException(
        s"Program linking failed with error: ${glGetProgramInfoLog(programId)}"
      )
    }
    
    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    new Shader(programId)
  }

  lazy val standard = create(
    vertexShaderSourcePath = "shaders/standard.vert",
    fragmentShaderSourcePath = "shaders/standard.frag"
  )
}
