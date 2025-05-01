package engine.graphics.opengl

import engine.graphics.*
import engine.graphics.config.WindowConfig
import engine.graphics.opengl.data.{ShaderProgram, ShaderSource}
import engine.graphics.opengl.shaders.ShaderUtils
import engine.graphics.opengl.data.*
import engine.graphics.opengl.shaders.*
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

object API extends GraphicsAPI {
  override def init(): Unit = {
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW")
    }

    glfwDefaultWindowHints() // resets all 'hints' to default values
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // creates invisible window at first
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE) // allows the window to be resizable
  }
  
  override def close(): Unit = glfwTerminate()
  
  override def createWindow(cfg: WindowConfig): WindowHandle = {
    val window = glfwCreateWindow(cfg.width, cfg.height, cfg.title, NULL, NULL)
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window")
    }

    // appear in the center of the monitor
    val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
    glfwSetWindowPos(
      window,
      (vidMode.width() - cfg.width) / 2,
      (vidMode.height() - cfg.height) / 2
    )

    glfwShowWindow(window)

    glfwMakeContextCurrent(window)
    createCapabilities()
    
    new WindowHandle {
      override def isActive: Boolean = !glfwWindowShouldClose(window)
      override def close(): Unit = glfwDestroyWindow(window)

      override def clearScreen(): Unit = {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
      }

      override def swapBuffers(): Unit = glfwSwapBuffers(window)
    }
  }
  
  override def createInputSystem(): InputSystem = () => glfwPollEvents()

  override def loadAsset(graphicAsset: GraphicAsset): GraphicHandle = {
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
    
    glBindVertexArray(vao)
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    
    val buffer = BufferUtils.createFloatBuffer(graphicAsset.vertexData.vertices.length)
    buffer.put(graphicAsset.vertexData.vertices).flip()
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
    glEnableVertexAttribArray(0)

    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)
    
    val shaderProgram = ShaderProgram.unwrap(ShaderUtils.createShaderProgram(
      ShaderSource(graphicAsset.shader.vertexShader),
      ShaderSource(graphicAsset.shader.fragmentShader),
    ))
    graphicAsset.shader.initialize(shaderProgram)
    
    new GraphicHandle {
      override def unload(): Unit = glDeleteProgram(shaderProgram)

      override def render(): Unit = {
        glUseProgram(shaderProgram)

        graphicAsset.shader.render()
        
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, graphicAsset.vertexData.vertexCount)
        glBindVertexArray(vao)
        
        glUseProgram(0)
      }
    }
  }
}
