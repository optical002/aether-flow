package aetherflow.renderEngine

import aetherflow.engine.graphics.Util
import aetherflow.engine.graphics.data.{Camera, Mat4f, Shader, Vec3f}
import aetherflow.engine.utils.Resources
import org.joml.*
import org.lwjgl.*
import org.lwjgl.stb.STBImage.*
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
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*

object Main {
  val screenWidth = 800
  val screenHeight = 600
  // 3d box
  val vertices: Array[Float] = Array[Float](
    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
    0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
    -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
    -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
    -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
    0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
    -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
  )

  val cubePositions: Array[Vec3f] = Array[Vec3f](
    Vec3f( 0.0f, 0.0f, 0.0f),
    Vec3f( 2.0f, 5.0f, -15.0f),
    Vec3f(-1.5f, -2.2f, -2.5f),
    Vec3f(-3.8f, -2.0f, -12.3f),
    Vec3f( 2.4f, -0.4f, -3.5f),
    Vec3f(-1.7f, 3.0f, -7.5f),
    Vec3f( 1.3f, -2.0f, -2.5f),
    Vec3f( 1.5f, 2.0f, -2.5f),
    Vec3f( 1.5f, 0.2f, -1.5f),
    Vec3f(-1.3f, 1.0f, -1.5f),
  )



  def main(args: Array[String]): Unit = {
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW")
    }

    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    val window = glfwCreateWindow(screenWidth, screenHeight, "Hello World!", NULL, NULL)
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window")
    }

    glfwMakeContextCurrent(window)
    createCapabilities()

    glViewport(0, 0, screenWidth, screenHeight)

    // Triangle start
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
//    val ebo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    val vboBuffer = BufferUtils.createFloatBuffer(vertices.length)
    vboBuffer.put(vertices).flip()
    glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW)

//    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
//    val eboBuffer = BufferUtils.createIntBuffer(indices.length)
//    eboBuffer.put(indices).flip()
//    glBufferData(GL_ELEMENT_ARRAY_BUFFER, eboBuffer, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
    glEnableVertexAttribArray(1)


    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    val standardShader = Shader.standard
    // Triangle end

//    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE) // <- wireframe
//    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL) // <- solid

    val texture1Id = Util.loadTexture(Resources.getPath("textures/wall.png"))
    val texture2Id = Util.loadTexture(Resources.getPath("textures/f_sleep.png"))

    standardShader.use()
    standardShader.setInt("texture1", 0)
    standardShader.setInt("texture2", 1)

    glEnable(GL_DEPTH_TEST)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    val camera = Camera.create(
      config = Camera.Config(movementSpeed = 0.1f),
      position = Vec3f(0, 0, 3),
      window = window
    )

    val matBuilderResource = Mat4f.Builder.instances.takeEntry
    val matBuilder = matBuilderResource.resource

    while (!glfwWindowShouldClose(window)) {
      // input
      processInput(window, camera)

      // rendering commands here
      glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      standardShader.use()

      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, texture2Id)
      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, texture1Id)
      glBindVertexArray(vao)

      def remap01(value: Float, min: Float, max: Float): Float = {
        min + (max - min) * value
      }
      val timeValue = glfwGetTime()

      val view = matBuilder
        .loadIdentity
        .viewFromCamera(camera)
      standardShader.setMat4f("view", view)

      val projection = matBuilder
        .loadIdentity
        .perspective(
          fov = Math.toRadians(camera.getZoom),
          aspect = screenWidth / screenHeight,
          near = 0.1f,
          far = 100.0f
        )
      standardShader.setMat4f("projection", projection)

      var i = 0
      for (cubePosition <- cubePositions) {
        val angle = 20.0f * i
        i += 1
        val model = matBuilder
          .loadIdentity
          .translate(cubePosition)
          .rotate(
            Math.toRadians(angle),
            new Vec3f(0.5f, 1.0f, 0.0f)
          )
        standardShader.setMat4f("model", model)
        glDrawArrays(GL_TRIANGLES, 0, 36)
      }

      glUseProgram(0)

      // check and call events and swap the buffers
      glfwSwapBuffers(window)
      glfwPollEvents()
    }

    matBuilderResource.release()
    glfwTerminate()
  }

  def processInput(window: Long, camera: Camera): Unit = {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, true)
    }

    camera.processInput(window)
  }
}
