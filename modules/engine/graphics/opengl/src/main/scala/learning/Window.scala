package learning

import engine.graphics.opengl.data.{ShaderProgram, ShaderSource}
//import engine.graphics.shaders.ShaderUtils
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
import org.lwjgl.system.MemoryUtil.*

object Window {
  def main(args: Array[String]): Unit = {
    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window = glfwCreateWindow(800, 600, "LearnOpenGL", NULL, NULL)
    if (window == NULL) {
      println("Failed to create GLFW window")
      glfwTerminate()
    }

    glfwMakeContextCurrent(window)

    // Ensures rendering scales with the new window size.
    glfwSetFramebufferSizeCallback(window, (win: Long, width: Int, height: Int) => {
      glViewport(0, 0, width, height)
    })

    GL.createCapabilities()

    while(!glfwWindowShouldClose(window)) {
      processInput(window)

      glClearColor(0.2f, 0.3f, 0.3f, 1.0f) // Configure clear buffer
      glClear(GL_COLOR_BUFFER_BIT) // Whole screen gets cleared with this ^ color.

      glfwSwapBuffers(window) // Doubled buffer
      glfwPollEvents()
    }

    glfwTerminate()
  }

  def processInput(window: Long): Unit = {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, true)
    }
  }

  def triangle() = {
    val shaderProgram = -1//ShaderProgram.unwrap(ShaderUtils.createShaderProgram(vertexShaderSource, fragmentShaderSource))

    val vbo = glGenBuffers() // creates a buffer
    val vao = glGenVertexArrays() // creates a backend for vbo, allows to directly access buffer data on the GPU
    // 1. bind vertex array object.
    glBindVertexArray(vao)

    // 2. Upload vertices into the GPU.
    glBindBuffer(GL_ARRAY_BUFFER, vbo) // binds a buffer to opengl state machine as current
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW) // puts all the vertices on the buffer into the GPU

    // 3. link the vertex attributes pointers
    glVertexAttribPointer(
      // The location on the shader 'layout(location = 0)' tells us we are dealing with data from shader in that place.
      0,
      // We tell from how many variables is the vertex data composed, in this case it's 3 for x, y and z.
      3,
      // The type in the single vertex. float for x, y and z.
      GL_FLOAT,
      // Is data normalized between 1 and 0.
      false,
      // A stride, how much data does vertex have, this also includes space in between, but in our case there is
      // no space.
      3 * 4,
      // An offset from where data begins inside the buffer.
      0
    ) // how opengl should interpret data on the gpu
    glEnableVertexAttribArray(0)

    // ...
    // Drawing
    glUseProgram(shaderProgram)
    glBindVertexArray(vao)
    




  }

  val vertices = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f, 0.5f, 0.0f,
  )

  val vertexShaderSource = ShaderSource(
    s"""
       |#version 330 core
       |layout (location = 0) in vec3 aPos;
       |
       |void main() {
       |  gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
       |}
       |""".stripMargin
  )

  val fragmentShaderSource = ShaderSource(
    s"""
       |#version 330 core
       |out vec4 FragColor;
       |
       |void main() {
       |  FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
       |}
       |""".stripMargin
  )
}
