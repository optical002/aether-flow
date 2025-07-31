package aetherflow.engine

import kyo.*
import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.nativelink.gl.glad.*
import aetherflow.engine.nativelink.gl.GLFW.*
import aetherflow.engine.nativelink.gl.GLFWExtras.*
import aetherflow.engine.nativelink.stb_image.*
import aetherflow.engine.graphics.data.{Shader, SpriteRenderer, Texture2D, Vec2f, Vec3f}
import aetherflow.engine.syntax.*
import aetherflow.engine.utils.Resources

import scalanative.unsafe.*
import scalanative.unsigned.*

object NativeMain {
  def keyCallback(window: Ptr[GLFWWindow], key: CInt, scancode: CInt, action: CInt, mods: CInt): Unit = {
    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, GLFW_TRUE)
    }
  }

  def errorCallback(error: CInt, description: CString) = {
    println(s"Error from GLFW $error: $description")
  }

  def main(args: Array[String]): Unit = Zone.acquire { implicit _ =>
    println("Starting GLFW window...")
    glfwSetErrorCallback(errorCallback.asCFunc)
    if (!glfwInit()) {
      println("GLFW failed to initialize, exiting...")
      sys.exit()
    }

    println("Creating GLFW window...")
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    val window = glfwCreateWindow(1280, 1080, c"Hello World", null, null)
    if (window == null) {
      println("Failed to create a GLFW window, exiting...")
      glfwTerminate()
      sys.exit()
    }

    println("Initializing GLFW window...")
    glfwSetKeyCallback(window, keyCallback.asCFunc)

    glfwMakeContextCurrent(window)
    gladLoadGL(glfwGetProcAddress)
    glfwSwapInterval(1)

    val spriteRenderer = SpriteRenderer.create(Shader.standard)
    val texture = Texture2D.load("textures/box.png")

    println("Entering rendering loop...")
    while (glfwWindowShouldClose(window) == 0) {
      val (width, height) = glfwGetFramebufferSize_(window)
      glViewport(0, 0, width = width.toUInt, height = height.toUInt)
      glClear(GL_COLOR_BUFFER_BIT)

      spriteRenderer.drawSprite(texture)
      glfwSwapBuffers(window)
      glfwPollEvents()
    }


    glfwDestroyWindow(window)
    glfwTerminate()
    println("Successfully closed window")
  }

  // Architecture:
  // SpriteRenderer(Shader) <- contains instructions for drawing sprite
  // |--> drawSprite(Texture2D, Position, Size, Rotate, Color)
  // Particle(Position, Velocity, Color, Life)
  // ResourceManager
  // PostProcessor
  // SoundEngine
  // TextRenderer
}
