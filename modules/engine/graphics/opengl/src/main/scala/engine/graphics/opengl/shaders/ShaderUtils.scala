package engine.graphics.opengl.shaders

import engine.graphics.opengl.data.*
import engine.graphics.opengl.data.{CompiledShader, ShaderCompileKind, ShaderProgram, ShaderSource}
import org.lwjgl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*

object ShaderUtils {
  def compileShader(src: ShaderSource, kind: ShaderCompileKind): CompiledShader = {
    val shaderId = glCreateShader(kind.asGlfwValue)
    glShaderSource(shaderId, ShaderSource.unwrap(src))
    glCompileShader(shaderId)

    val status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
    if (status != GL_TRUE) {
      val log = glGetShaderInfoLog(shaderId)
      throw new RuntimeException(s"Shader compilation failed: $log")
    }

    CompiledShader(shaderId)
  }

  def createShaderProgram(vertex: ShaderSource, fragment: ShaderSource): ShaderProgram = {
    val compiledVertex = compileShader(vertex, ShaderCompileKind.Vertex)
    val compiledFragment = compileShader(fragment, ShaderCompileKind.Fragment)

    val program = glCreateProgram()
    glAttachShader(program, CompiledShader.unwrap(compiledVertex))
    glAttachShader(program, CompiledShader.unwrap(compiledFragment))
    glLinkProgram(program)

    val status = glGetProgrami(program, GL_LINK_STATUS)
    if (status != GL_TRUE) {
      val log = glGetProgramInfoLog(program)
      throw new RuntimeException(s"Shader linking failed: $log")
    }

    glDeleteShader(CompiledShader.unwrap(compiledVertex))
    glDeleteShader(CompiledShader.unwrap(compiledFragment))

    ShaderProgram(program)
  }
}
