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

object Main {
  val vertices: Array[Float] = Array[Float](
    // positions       // colors         // texture coords
     0.5f,  0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
     0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom right
    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom left
    -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
  )
  val indices: Array[Int] = Array[Int]( // note that we start from 0!
    0, 1, 3, // first triangle
    1, 2, 3, // second triangle
  )
  val texCoords: Array[Float] = Array[Float](
    0.0f, 0.0f, // lower-left corner
    1.0f, 0.0f, // lower-right corer
    0.5f, 1.0f, // top-center corner
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
    val window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL)
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window")
    }

    glfwMakeContextCurrent(window)
    createCapabilities()

    glViewport(0, 0, 800, 600)

    // Triangle start
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
    val ebo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    val vboBuffer = BufferUtils.createFloatBuffer(vertices.length)
    vboBuffer.put(vertices).flip()
    glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    val eboBuffer = BufferUtils.createIntBuffer(indices.length)
    eboBuffer.put(indices).flip()
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, eboBuffer, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
    glEnableVertexAttribArray(2)


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

    while (!glfwWindowShouldClose(window)) {
      // input
      processInput(window)

      // rendering commands here
      glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
      glClear(GL_COLOR_BUFFER_BIT)

      standardShader.use()

      val timeValue = glfwGetTime()
      val greenValue = ((math.sin(timeValue) / 2f) + 0.5f).asInstanceOf[Float]
      standardShader.setVec4f("ourColor", Vector4f(0.0f, greenValue, 0.0f, 1.0f))

      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, texture2Id)
      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, texture1Id)

      glBindVertexArray(vao)
      glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0)

      glUseProgram(0)

      // check and call events and swap the buffers
      glfwSwapBuffers(window)
      glfwPollEvents()
    }

    glfwTerminate()
  }

  def processInput(window: Long): Unit = {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, true)
    }
  }
}
