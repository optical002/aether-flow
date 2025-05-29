package aetherflow.engine.graphics.opengl

import aetherflow.engine.components.{Renderer, Transform}
import aetherflow.engine.components.Renderer.Initialized
import aetherflow.engine.ecs.EcsStateMachine
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.*
import aetherflow.engine.graphics.data.{Camera, Mat4f, Shader, Vec3f}
import aetherflow.engine.utils.Resources
import org.joml.*
import org.lwjgl.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.{GL_TEXTURE0, GL_TEXTURE1, glActiveTexture}
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import zio.{Task, UIO, ZIO}

object API extends GraphicsAPI {
  override def init(): Unit = {
    // https://github.com/glfw/glfw/issues/2680#issuecomment-2710866718
    println(
      "Note: If your window does not appear and you are on wayland session on linux, you " +
      "might want yo try setting `__GL_THREADED_OPTIMIZATIONS=0` env var"
    )
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

    glEnable(GL_DEPTH_TEST)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    
    val matBuilderEntry = Mat4f.Builder.instances.takeEntry
    val matBuilder = matBuilderEntry.resource
    
    val camera = Camera.create(
      config = Camera.Config(movementSpeed = 0.1f),
      position = Vec3f(0, 0, 3),
      window = window
    )

    new WindowHandle {
      override def isActive: Boolean = !glfwWindowShouldClose(window)
      override def close(): Unit = {
        matBuilderEntry.release()
        glfwDestroyWindow(window)
      }

      override def clearScreen(): Unit = {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
      }

      override def swapBuffers(): Unit = glfwSwapBuffers(window)
      override def processInput(): Unit = {
        glfwPollEvents()
        camera.processInput(window)
      }

      override def initializeRenderers(
        ecsStateMachine: EcsStateMachine
      ): UIO[Unit] = for {
        components <- ecsStateMachine.query1[Renderer]
        _ <- ZIO.foreach(components)((entity, rendererRef) => rendererRef.update { r =>
          val kind = r.kind match {
            case uninitialized: Renderer.Uninitialized =>
              val vertices = uninitialized.mesh.vertices
              
              // This should have these steps
              // - Look at all shaderSources, it should be a Set, then initialize them, 
              // load if new shader sources appear
              //   | Figure out what is this structure:
              //      - Vertices which create VAO
              //      - Vertices have more data in them like texCoords, and there can be more formats.
              //      - Textures depend on shader settings and.
              // - Have this structure initialize same way as shaders.
              // - Make renderer reuse this structure, for the same stuff.
              
              
              // TODO Problem recreates shader every time
              // TODO Problem rebinds everything, for same model, this should be per model not per renderer
              // TODO Problem this still is not abstract it's very specific to the box with standard shader
              val shader = Shader.create(uninitialized.shaderSource)

              val vao = glGenVertexArrays()
              val vbo = glGenBuffers()

              glBindVertexArray(vao)
              glBindBuffer(GL_ARRAY_BUFFER, vbo)
              val vboBuffer = BufferUtils.createFloatBuffer(vertices.length)
              vboBuffer.put(vertices).flip()
              glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW)

              glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
              glEnableVertexAttribArray(0)
              glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
              glEnableVertexAttribArray(1)

              glBindBuffer(GL_ARRAY_BUFFER, 0)
              glBindVertexArray(0)

              val texture1Id = Util.loadTexture(Resources.getPath("textures/wall.png"))
              val texture2Id = Util.loadTexture(Resources.getPath("textures/f_sleep.png"))

              shader.use()
              shader.setInt("texture1", 0)
              shader.setInt("texture2", 1)

              Initialized(vao, shader, texture1Id, texture2Id)
            case initialized: Renderer.Initialized => initialized
          }
          Renderer(kind)
        }.commit)
      } yield ()

      override def renderRenderers(
        ecsStateMachine: EcsStateMachine,
        windowConfig: WindowConfig
      ): Task[Unit] = for {
        components <- ecsStateMachine.query2[Transform, Renderer]
        _ <- ZIO.foreach(components) { case (entity, transformRef, rendererRef) =>
          val transaction = for {
            transform <- transformRef.get
            renderer <- rendererRef.get
          } yield (transform, renderer)

          for {
            (transform, renderer) <- transaction.commit
            _ <- ZIO.attempt(renderer.kind match {
              case Renderer.Uninitialized(mesh, shaderSource) => ()
              case Renderer.Initialized(vao, shader, texture1Id, texture2Id) =>

                shader.use()

                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, texture1Id)
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, texture2Id)

                glBindVertexArray(vao)

                val view = matBuilder
                  .loadIdentity
                  .viewFromCamera(camera)
                shader.setMat4f("view", view)

                val projection = matBuilder
                  .loadIdentity
                  .perspective(
                    fov = Math.toRadians(camera.getZoom),
                    aspect = windowConfig.aspectRatio,
                    near = 0.1f,
                    far = 100.0f
                  )
                shader.setMat4f("projection", projection)

                val model = matBuilder
                  .loadIdentity
                  .translate(transform.position)
                  .rotateEuler(transform.rotation)
                  .scale(transform.scale)
                shader.setMat4f("model", model)


                glDrawArrays(GL_TRIANGLES, 0, 36)

                glUseProgram(0)
            })
          } yield ()
        }
      } yield ()
    }
  }
}
