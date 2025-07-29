package aetherflow.engine

import kyo.*
import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.nativelink.gl.gl.*
import aetherflow.engine.nativelink.gl.GLFW.*
import aetherflow.engine.nativelink.gl.GLFWExtras.*
import aetherflow.engine.data.*
import aetherflow.engine.syntax.*

import scalanative.unsafe.*
import scalanative.unsigned.*

object NativeMain {
  case class Vertex(pos: Vec2f, col: Vec3f)

  val vertices = Array[GLfloat](
    -0.5f, -0.5f, 0.0f,
     0.5f, -0.5f, 0.0f,
     0.0f, 0.5f, 0.0f
  )

  def vertexShaderText(using Zone) =
    """
      |#version 330 core
      |layout (location = 0) in vec3 aPos;
      |void main() {
      |  gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0f);
      |}
      |""".stripMargin.asCString
  def fragmentShaderText(using Zone) =
    """
      |#version 330
      |out vec4 FragColor;
      |void main() {
      |  FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
      |}
      |""".stripMargin.asCString

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
      

      println("Creating vertex shader")
      val vertexShader = glCreateShader(GL_VERTEX_SHADER)
      val vertexShaderTextPtr = stackalloc[CString]()
      vertexShaderTextPtr.update(0, vertexShaderText)
      glShaderSource(vertexShader, 1.toUInt, vertexShaderTextPtr, null)
      glCompileShader(vertexShader)
      val status = stackalloc[GLint]()
      glGetShaderiv(vertexShader, GL_COMPILE_STATUS, status)
      println(s"status of vertex shader ${!status}")

      println("Creating fragment shader")
      val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
      val fragmentShaderTextPtr = stackalloc[CString]()
      fragmentShaderTextPtr.update(0, fragmentShaderText)
      glShaderSource(fragmentShader, 1.toUInt, fragmentShaderTextPtr, null)
      glCompileShader(fragmentShader)
      glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, status)
      println(s"status of vertex shader ${!status}")

      println("LinkingShaders")
      val program = glCreateProgram()
      glAttachShader(program, vertexShader)
      glAttachShader(program, fragmentShader)
      glLinkProgram(program)
      glGetProgramiv(program, GL_COMPILE_STATUS, status)
      println(s"status of shader program ${!status}")

      glUseProgram(program)

      println("Binding vertex array")
      val VAO = stackalloc[GLuint]()
      glGenVertexArrays(1.toUInt, VAO)
      glBindVertexArray(!VAO)

      println("Creating vertex buffer")
      val VBO = stackalloc[GLuint]();
      glGenBuffers(1.toUInt, VBO)
      glBindBuffer(GL_ARRAY_BUFFER, !VBO)

      val verticesPtr = stackalloc[GLfloat](vertices.length)
      for (i <- vertices.indices) {
        verticesPtr.update(i, vertices(i))
        println(s"$i = ${verticesPtr(i)}")
      }
      glBufferData(GL_ARRAY_BUFFER, vertices.length * 4, verticesPtr.asInstanceOf[Ptr[Byte]], GL_STATIC_DRAW)

      println("Binding VertexArray")
      glVertexAttribPointer(
        0.toUInt, 3, GL_FLOAT, GL_FALSE,
        stride = (3 * 4).toUInt,
        pointer = 0L.toPtr[Byte]
      )
      glEnableVertexAttribArray(0.toUInt)
      
      println("Entering rendering loop...")
      while (glfwWindowShouldClose(window) == 0) {
//        println("Rendering frame...")
        val (width, height) = glfwGetFramebufferSize_(window)
        glViewport(0, 0, width = width.toUInt, height = height.toUInt)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(program)
        glBindVertexArray(!VAO)
        glDrawArrays(GL_TRIANGLES, 0, 3.toUInt)

        glfwSwapBuffers(window)
        glfwPollEvents()
      }


      glfwDestroyWindow(window)
      glfwTerminate()
      println("Successfully closed window")
    }
  }
}
