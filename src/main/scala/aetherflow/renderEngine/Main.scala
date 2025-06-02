package aetherflow.renderEngine

import aetherflow.engine.graphics.Utils
import aetherflow.engine.graphics.data.{Camera, Mat4f, Model, Shader, Vec3f}
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
import org.lwjgl.assimp.*

object Main {
  val screenWidth = 1980
  val screenHeight = 1080
  // 3d box
  val vertices: Array[Float] = Array[Float](
    // positions          // normals           // texture coords
    -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
     0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  0.0f,
     0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
     0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
    -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  1.0f,
    -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
    -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,
     0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
    -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
    -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,
    -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
    -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
    -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
    -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
    -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
    -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
     0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
     0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
     0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
     0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
     0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
     0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
    -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,
     0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  1.0f,
     0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
     0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
    -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  0.0f,
    -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,
    -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f,
     0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  1.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
    -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,
    -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f
  )

  val cubePositions: Array[Vec3f] = Array[Vec3f](
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
  val pointLightPositions: Array[Vec3f] = Array[Vec3f](
    Vec3f( 0.7f,  0.2f,  2.0f),
    Vec3f( 2.3f, -3.3f, -4.0f),
    Vec3f(-4.0f,  2.0f, -12.0f),
    Vec3f( 0.0f,  0.0f, -3.0f)
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

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    val vboBuffer = BufferUtils.createFloatBuffer(vertices.length)
    vboBuffer.put(vertices).flip()
    glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
    glEnableVertexAttribArray(2)

    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    val cubeShader = Shader.create(
      "shaders/standard.vert",
      "shaders/standard.frag"
    )
    val lightShader = Shader.create(
      "shaders/standard.vert",
      "shaders/light.frag"
    )
    val lightPosition = Vec3f(1.2f, 1.0f, 2.0f)
    // Triangle end

    // Light
    val lightVAO = glGenVertexArrays()
    glBindVertexArray(lightVAO)
    glBindBuffer(GL_ARRAY_BUFFER, vbo)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
    glEnableVertexAttribArray(0)
    // end Light

    glEnable(GL_DEPTH_TEST)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    val camera = Camera.create(
      config = Camera.Config(movementSpeed = 0.1f),
      position = Vec3f(0, 0, 3),
      window = window
    )

    val matBuilderResource = Mat4f.Builder.instances.takeEntry
    val matBuilder = matBuilderResource.resource

    val diffuseMap = Utils.loadTexture(Resources.getPath("textures/container2.png"))
    val specularMap = Utils.loadTexture(Resources.getPath("textures/container2_specular.png"))
    
    val ourModel = Model.importModel_v2(Resources.getPath("models/boxModel1/Crate/Crate1.obj"))
//    val ourModel = Model.importModel_v2(Resources.getPath("models/boxModel2/box.fbx"))
//    val ourModel = Model.importModel_v2(Resources.getPath("models/katana/katana.obj"))
    val ourModel2 = Model.importModel_v2(Resources.getPath("models/katana2/KatanaForZIP.obj"))

    while (!glfwWindowShouldClose(window)) {
      // input
      processInput(window, camera)

      // rendering commands here
      glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      val timeValue = glfwGetTime()

      cubeShader.use()
      // Setting view Pos
      cubeShader.setVec3f("viewPos", camera.getPosition)

      // Setting material
      cubeShader.setVec3f("material.ambient", Vec3f(1.0f, 0.5f, 0.31f))
      cubeShader.setInt("texture_diffuse", 0)
      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, diffuseMap)
      cubeShader.setInt("texture_specular", 1)
      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, specularMap)
      cubeShader.setFloat("material.shininess", 32.0f)
      cubeShader.setUniform3f("material.ambientColor", 1, 1, 1)
      cubeShader.setUniform3f("material.diffuseColor", 1, 1, 1)
      cubeShader.setUniform3f("material.specularColor", 1, 1, 1)

      // Dir light
      cubeShader.setVec3f("dirLight.direction", Vec3f(-0.2f, -1.0f, -0.3f))
      cubeShader.setVec3f("dirLight.ambient", Vec3f(0.05f))
      cubeShader.setVec3f("dirLight.diffuse", Vec3f(0.4f))
      cubeShader.setVec3f("dirLight.specular", Vec3f(0.5f))

      // Point Lights
      var i = 0
      for (pointLightPosition <- pointLightPositions) {
        cubeShader.setVec3f(s"pointLights[$i].position", pointLightPosition)

        cubeShader.setFloat(s"pointLights[$i].constant", 1.0f)
        cubeShader.setFloat(s"pointLights[$i].linear", 0.7f)
        cubeShader.setFloat(s"pointLights[$i].quadratic", 1.8f)

        cubeShader.setVec3f(s"pointLights[$i].ambient", Vec3f(0.2f))
        cubeShader.setVec3f(s"pointLights[$i].diffuse", Vec3f(0.5f))
        cubeShader.setVec3f(s"pointLights[$i].specular", Vec3f(1.0f))
        i += 1
      }

      // Spot Light
      cubeShader.setVec3f("spotLight.position", camera.getPosition)
      cubeShader.setVec3f("spotLight.direction", camera.getFront)
      cubeShader.setFloat("spotLight.cutoff", Math.cos(Math.toRadians(12.5f)))
      cubeShader.setFloat("spotLight.outerCutoff", Math.cos(Math.toRadians(17.5f)))

      cubeShader.setVec3f("spotLight.ambient", Vec3f(0.2f))
      cubeShader.setVec3f("spotLight.diffuse", Vec3f(0.5f))
      cubeShader.setVec3f("spotLight.specular", Vec3f(1.0f))

      cubeShader.setFloat("spotLight.constant", 1.0f)
      cubeShader.setFloat("spotLight.linear", 0.14f)
      cubeShader.setFloat("spotLight.quadratic", 0.0007f)

      glBindVertexArray(vao)

      val view = matBuilder
        .loadIdentity
        .viewFromCamera(camera)
        .build
      cubeShader.setMat4f("view", view)

      val projection = matBuilder
        .loadIdentity
        .perspective(
          fov = Math.toRadians(camera.getZoom),
          aspect = screenWidth.toFloat / screenHeight,
          near = 0.1f,
          far = 100.0f
        )
        .build
      cubeShader.setMat4f("projection", projection)

      i = 0
      for (cubePosition <- cubePositions) {
        val angle = 20.0f * i
        i += 1
        cubeShader.setMat4f("model", matBuilder
          .loadIdentity
          .translate(cubePosition)
          .rotate(
            Math.toRadians(angle),
            new Vec3f(0.5f, 1.0f, 0.0f).normalize
          )
        )
        glDrawArrays(GL_TRIANGLES, 0, 36)
      }
      cubeShader.setMat4f("model", matBuilder
        .loadIdentity
        .translate(Vec3f(0.0f, 0.0f, -10.0f))
//        .scale(0.025f)
      )
      ourModel.draw(cubeShader)
      cubeShader.setMat4f("model", matBuilder
        .loadIdentity
        .translate(Vec3f(0.0f, 5.0f, -10.0f))
      )
      ourModel2.draw(cubeShader)

      lightShader.use()
      lightShader.setMat4f("projection", projection)
      lightShader.setMat4f("view", view)
      glBindVertexArray(lightVAO)
      for (pointLight <- pointLightPositions) {
        val model = matBuilder
          .loadIdentity
          .translate(pointLight)
          .scale(0.2f)
        lightShader.setMat4f("model", model)
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
