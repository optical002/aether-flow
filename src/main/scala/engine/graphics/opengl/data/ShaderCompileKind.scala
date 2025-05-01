package engine.graphics.opengl.data

import org.lwjgl.opengl.GL20.*

sealed trait ShaderCompileKind(val asGlfwValue: Int)
object ShaderCompileKind {
  object Vertex extends ShaderCompileKind(GL_VERTEX_SHADER)
  object Fragment extends ShaderCompileKind(GL_FRAGMENT_SHADER)
}
