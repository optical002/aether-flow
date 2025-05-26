package aetherflow.renderEngine

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
import aetherflow.math.*

object Main {
  val screenWidth = 800
  val screenHeight = 600
//  val vertices: Array[Float] = Array[Float](
//    // positions       // colors         // texture coords
//     0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
//     0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom right
//    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom left
//    -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
//  )
//  val indices: Array[Int] = Array[Int]( // note that we start from 0!
//    0, 1, 3, // first triangle
//    1, 2, 3, // second triangle
//  )
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
  val texCoords: Array[Float] = Array[Float](
    0.0f, 0.0f, // lower-left corner
    1.0f, 0.0f, // lower-right corer
    0.5f, 1.0f, // top-center corner
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

  def loadTexture(path: String): Int = {
    val stack = MemoryStack.stackPush()
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    val channels = stack.mallocInt(1)

    stbi_set_flip_vertically_on_load(true)
    val image = stbi_load(path, width, height, channels, 4) // Force RGBA
    if (image == null) {
      throw new RuntimeException(s"Failed to load image: ${stbi_failure_reason()}")
    }

    val textureId = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, textureId)
    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA,
      width.get(0),
      height.get(0),
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      image
    )
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glGenerateMipmap(GL_TEXTURE_2D)

    glBindTexture(GL_TEXTURE_2D, 0)
    stbi_image_free(image)
    stack.close()
    textureId
  }

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

    val texture1Id = loadTexture(Resources.getPath("textures/wall.png"))
    val texture2Id = loadTexture(Resources.getPath("textures/f_sleep.png"))

    standardShader.use()
    standardShader.setInt("texture1", 0)
    standardShader.setInt("texture2", 1)

    glEnable(GL_DEPTH_TEST)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    val camera = Camera.create(
      config = Camera.Config(movementSpeed = 0.1f),
      position = Vec3f(0, 0, 3),
      window =window
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
