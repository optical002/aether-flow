package aetherflow.engine

import kyo.*

import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.gl.*
import aetherflow.engine.nativelink.gl.GLFW.*
import aetherflow.engine.nativelink.gl.GLFWExtras.*
import aetherflow.engine.syntax.*

import scalanative.unsafe.*
import scalanative.unsigned.*

object NativeMain {
  type Vec2 = CStruct2[Float, Float]
  object Vec2 {
    def apply(x: Float, y: Float): Vec2 = {
      val vec2 = !stackalloc[Vec2]()
      vec2._1 = x
      vec2._2 = y
      vec2
    }
  }
  type Vec3 = CStruct3[Float, Float, Float]
  object Vec3 {
    def apply(x: Float, y: Float, z: Float): Vec3 = {
      val vec3 = !stackalloc[Vec3]()
      vec3._1 = x
      vec3._2 = y
      vec3._3 = z
      vec3
    }
  }
  type Vertex = CStruct2[Vec2, Vec3]
  object Vertex {
    def apply(pos: Vec2, col: Vec3): Vertex = {
      val vertex = !stackalloc[Vertex]()
      vertex._1 = pos
      vertex._2 = col
      vertex
    }
  }

  val vertices = Array {
    Vertex(pos = Vec2(x = -0.6, y = -0.4), col = Vec3(x = 1, y = 0, z = 0))
    Vertex(pos = Vec2(x =  0.6, y = -0.4), col = Vec3(x = 0, y = 1, z = 0))
    Vertex(pos = Vec2(x =    0, y =  0.6), col = Vec3(x = 0, y = 0, z = 1))
  }

  def keyCallback(window: Ptr[GLFWWindow], key: CInt, scancode: CInt, action: CInt, mods: CInt): Unit = {
    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, GLFW_TRUE)
    }
  }

  def errorCallback(error: CInt, description: CString) = {
    println(s"Error from GLFW $error: $description")
  }

  def main(args: Array[String]): Unit = {
    Zone.acquire { implicit (z: Zone) =>
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
      val window = glfwCreateWindow(640, 320, "Hello World".asCString, null, null)
      if (window == null) {
        println("Failed to create a GLFW window, exiting...")
        sys.exit()
      }

      println("Initializing GLFW window...")
      glfwSetKeyCallback(window, keyCallback.asCFunc)
      glfwMakeContextCurrent(window)
      val version = gladLoadGL(glfwGetProcAddress)
      println(s"GladLoad with version: $version")
      glfwSwapInterval(1)

      println("Entering rendering loop...")
      while (glfwWindowShouldClose(window) == 0) {
//        println("Rendering frame...")

        val (width, height) = glfwGetFramebufferSize_(window)
        glViewport(0, 0, width = width.toUInt, height = height.toUInt)
        glfwSwapBuffers(window)
        glfwPollEvents()
      }

      glfwDestroyWindow(window)
      glfwTerminate()
      println("Successfully closed window")
    }
  }
}
