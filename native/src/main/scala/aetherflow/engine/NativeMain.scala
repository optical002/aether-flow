package aetherflow.engine

import kyo.*

import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLFW.*
import aetherflow.engine.nativelink.gl.GLFWExtras.*
import aetherflow.engine.syntax.*

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
      glfwSwapInterval(1)

      println("Entering rendering loop...")
      while (glfwWindowShouldClose(window) == 0) {
        println("Rendering frame...")

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
