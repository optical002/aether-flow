package engine.graphics

import engine.components.*
import engine.ecs.*
import engine.core.*
import engine.core.FrameCoordinator.SignalFrom.Render
import engine.graphics.*
import engine.graphics.config.*
import zio.*

import java.util.concurrent.Executors

object Window {
  private val logger = new Logger("Window")

  val singleThreadExecutor: UIO[Executor] =
    ZIO.attempt(Executors.newSingleThreadExecutor()).map(Executor.fromJavaExecutor).orDie

  def run = (for {
    _ <- logger.logVerbose("Creating single thread executor")
    exec <- singleThreadExecutor
    _ <- logger.logVerbose("Starting window program on single thread")
    _ <- windowProgram.onExecutor(exec)
  } yield ()).fork

  private def windowProgram = for {
    cfg <- ZIO.service[WindowConfig]
    api <- ZIO.service[GraphicsAPI]
    db <- ZIO.service[GraphicDatabase]
    frameCoordinator <- ZIO.service[FrameCoordinator]
    _ <- logger.logVerbose("Initializing graphics api")
    _ <- ZIO.attempt(api.init())
    _ <- logger.logVerbose("Creating window")
    window <- ZIO.attempt(api.createWindow(cfg))
    _ <- logger.logVerbose("Creating input system")
    inputSystem <- ZIO.attempt(api.createInputSystem())
    clock <- ZIO.clock
    _ <- {
      def loop(): Task[Unit] = {
        if (!window.isActive) logger.logVerbose("Window became inactive")
        else for {
          _ <- logger.logVerbose("Waiting for next frame")
          _ <- frameCoordinator.signalReady(Render)
          _ <- logger.logVerbose("Initializing queued up assets")
          _ <- db.initializeQueuedUpAssets()
          _ <- logger.logVerbose("Polling events")
          _ <- ZIO.attempt(inputSystem.pollEvents())
          _ <- logger.logVerbose("Clearing screen")
          _ <- ZIO.attempt(window.clearScreen())
          _ <- logger.logVerbose("Rendering all assets")
          _ <- db.renderAllAssets()
          _ <- logger.logVerbose("Swapping buffers")
          _ <- ZIO.attempt(window.swapBuffers())
          _ <- loop()
        } yield ()
      }

      loop()
    }
    _ <- logger.logVerbose("Closing window")
    _ <- ZIO.attempt(window.close())
    _ <- logger.logVerbose("Unloading all assets")
    _ <- db.unloadAll()
    _ <- logger.logVerbose("Closing graphics api")
    _ <- ZIO.attempt(api.close())
  } yield ()
}
