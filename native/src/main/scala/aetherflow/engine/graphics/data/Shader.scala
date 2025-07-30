package aetherflow.engine.graphics.data

import aetherflow.engine.utils.Resources
import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.syntax.*

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

class Shader private(val programId: GLuint) {
  def use(): Unit =
    glUseProgram(programId)

  def setBool(name: CString, value: Boolean): Unit =
    glUniform1i(glGetUniformLocation(programId, name), if (value) 1 else 0)

  def setInt(name: CString, value: Int): Unit =
    glUniform1i(glGetUniformLocation(programId, name), value)

  def setFloat(name: CString, value: Float): Unit =
    glUniform1f(glGetUniformLocation(programId, name), value)

  def setVec3f(name: CString, value: Vec3f): Unit = {
    glUniform3f(glGetUniformLocation(programId, name), value.x, value.y, value.z)
  }

  def setUniform3f(name: CString, f1: Float, f2: Float, f3: Float): Unit = {
    glUniform3f(glGetUniformLocation(programId, name), f1, f2, f3)
  }

  def setUniform4f(name: CString, f1: Float, f2: Float, f3: Float, f4: Float): Unit = {
    glUniform4f(glGetUniformLocation(programId, name), f1, f2, f3, f4)
  }

//  def setMat4f(name: CString, value: Mat4f.Builder): Unit = {
////    val stack = MemoryStack.stackPush()
////    val fb = stack.mallocFloat(16)
////    value.fill(fb)
////    glUniformMatrix4fv(glGetUniformLocation(programId, name), false, fb)
////    stack.close()
//  }
//
//  def setMat4f(name: String, value: Mat4f): Unit = {
////    val stack = MemoryStack.stackPush()
////    val fb = stack.mallocFloat(16)
////    value.fill(fb)
////    glUniformMatrix4fv(glGetUniformLocation(programId, name), false, fb)
////    stack.close()
//  }
}
object Shader {
  def create(source: ShaderSource): Shader = create(
    vertexShaderSourcePath = source.vertPath,
    fragmentShaderSourcePath = source.fragPath
  )

  private def compileShader(shader: GLuint, shaderSource: CString, path: String): Unit = {
    glShaderSource_(shader, shaderSource)
    glCompileShader(shader)
    if (!glGetShaderi(shader, GL_COMPILE_STATUS)) {
      throw new RuntimeException(
        s"Compiling shader at path=$path, failed with error: ${glGetShaderInfoLog_(shader)}"
      )
    }
  }

  def create(
    vertexShaderSourcePath: String,
    fragmentShaderSourcePath: String
  ): Shader = { Zone.acquire { implicit z =>
    val vertexShaderSource = Resources.readText(path = vertexShaderSourcePath).asCString
    val fragmentShaderSource = Resources.readText(path = fragmentShaderSourcePath).asCString

    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    compileShader(vertexShader, vertexShaderSource, vertexShaderSourcePath)

    val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
    compileShader(fragmentShader, fragmentShaderSource, fragmentShaderSourcePath)

    val programId = glCreateProgram()
    glAttachShader(programId, vertexShader)
    glAttachShader(programId, fragmentShader)
    glLinkProgram(programId)
    if (!glGetProgrami(programId, GL_LINK_STATUS)) {
      throw new RuntimeException(
        s"Program linking failed with error: ${glGetProgramInfoLog_(programId)} ..."
      )
    }

    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    new Shader(programId)
  }}

  lazy val standard: Shader = create(
    vertexShaderSourcePath = "shaders/standard.vert",
    fragmentShaderSourcePath = "shaders/standard.frag"
  )
}

