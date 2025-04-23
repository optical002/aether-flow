package engine.graphics

import engine.components.*
import engine.ecs.*
import engine.core.*
import engine.graphics.*
import engine.graphics.config.*
import zio.*

import java.util.concurrent.Executors

object Window {
  val singleThreadExecutor: UIO[Executor] =
    ZIO.attempt(Executors.newSingleThreadExecutor()).map(Executor.fromJavaExecutor).orDie

  def runProgram(barrier: FrameCoordinator.Barrier) = (for {
    exec <- singleThreadExecutor
    _ <- windowProgram(barrier).onExecutor(exec)
  } yield ()).fork

  private def windowProgram(barrier: FrameCoordinator.Barrier) = for {
    cfg <- ZIO.service[WindowConfig]
    api <- ZIO.service[GraphicsAPI]
    db <- ZIO.service[GraphicDatabase]
    _ <- ZIO.attempt(api.init())
    window <- ZIO.attempt(api.createWindow(cfg))
    inputSystem <- ZIO.attempt(api.createInputSystem())
    _ <- {
      def loop(): Task[Unit] = {
        if (!window.isActive) ZIO.unit
        else for {
          _ <- ZIO.succeed("Window Frame Start").debug
          _ <- db.initializeQueuedUpAssets()
          _ <- ZIO.attempt(inputSystem.pollEvents())
          _ <- ZIO.attempt(window.clearScreen())
          _ <- db.renderAllAssets()
          _ <- ZIO.attempt(window.swapBuffers())
          _ <- ZIO.succeed("Window Frame End").debug
          _ <- barrier
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
