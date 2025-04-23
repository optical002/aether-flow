package engine.graphics

import engine.components.*
import engine.ecs.WorldBuilder
import engine.graphics.*
import engine.graphics.config.*
import zio.*

object Window {
  def forkWindow = ZIO.scoped(for {
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
//          _ <- ZIO.sleep(16.millis)
          _ <- loop()
        } yield ()
      }

      loop()
    }
    _ <- ZIO.attempt(window.close())
    _ <- db.unloadAll()
    _ <- ZIO.attempt(api.close())
  } yield ()).fork // Have a dedicated thread for this window and do not swap.
}
