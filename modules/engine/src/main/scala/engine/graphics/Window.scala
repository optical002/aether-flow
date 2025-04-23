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
  val singleThreadExecutor: UIO[Executor] =
    ZIO.attempt(Executors.newSingleThreadExecutor()).map(Executor.fromJavaExecutor).orDie

  def run = (for {
    exec <- singleThreadExecutor
    _ <- windowProgram.onExecutor(exec)
  } yield ()).fork

  private def windowProgram = for {
    cfg <- ZIO.service[WindowConfig]
    api <- ZIO.service[GraphicsAPI]
    db <- ZIO.service[GraphicDatabase]
    frameCoordinator <- ZIO.service[FrameCoordinator]
    _ <- ZIO.attempt(api.init())
    window <- ZIO.attempt(api.createWindow(cfg))
    inputSystem <- ZIO.attempt(api.createInputSystem())
    clock <- ZIO.clock
    started <- clock.nanoTime
    _ <- {
      def loop(): Task[Unit] = {
        if (!window.isActive) ZIO.unit
        else for {
          _ <- frameCoordinator.signalReady(Render)
          _ <- db.initializeQueuedUpAssets()
          _ <- ZIO.attempt(inputSystem.pollEvents())
          _ <- ZIO.attempt(window.clearScreen())
          _ <- db.renderAllAssets()
          _ <- ZIO.attempt(window.swapBuffers())
          _ <- loop()
        } yield ()
      }

      loop()
    }
    _ <- ZIO.attempt(window.close())
    _ <- db.unloadAll()
    _ <- ZIO.attempt(api.close())
  } yield ()
}
