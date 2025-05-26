package aetherflow.engine.components

import aetherflow.engine.ecs.Component
import aetherflow.engine.graphics.Util
import aetherflow.engine.graphics.data.*
import aetherflow.engine.utils.Resources
import org.joml.*
import org.lwjgl.*
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
import org.lwjgl.system.MemoryUtil.*

case class Renderer(kind: Renderer.Kind) extends Component

object Renderer {
  def uninitialized(
    mesh: Mesh, shaderSource: ShaderSource
  ): Renderer = Renderer(Uninitialized(mesh, shaderSource))

  sealed trait Kind
  case class Uninitialized(mesh: Mesh, shaderSource: ShaderSource) extends Kind
  case class Initialized(vao: Int, shader: Shader, texture1: Int, texture2: Int) extends Kind
}

