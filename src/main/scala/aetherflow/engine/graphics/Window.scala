package aetherflow.engine.graphics

import aetherflow.engine.core.FrameCoordinator
import aetherflow.engine.core.FrameCoordinator.SignalFrom
import aetherflow.engine.core.logger.ASyncLogger
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.performance.Performance
import aetherflow.engine.performance.PerformanceMetrics.*
import zio.*

import java.util.concurrent.Executors

object Window {
  private val logger = new ASyncLogger("Window")

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
    clock <- ZIO.clock
    start <- clock.nanoTime
    frameCoordinator <- ZIO.service[FrameCoordinator]
    _ <- logger.logVerbose("Initializing graphics api")
    _ <- ZIO.attempt(api.init())
    _ <- logger.logVerbose("Creating window")
    window <- ZIO.attempt(api.createWindow(cfg))
    _ <- logger.logVerbose("Creating input system")
    inputSystem <- ZIO.attempt(api.createInputSystem())
    end <- clock.nanoTime
    _ <- ZIO.succeed[Double](end - start) @@ Performance.labelNs(windowStartup)
    _ <- {
      def loop(): Task[Unit] = {
        if (!window.isActive) logger.logVerbose("Window became inactive")
        else for {
          _ <- logger.logVerbose("Waiting for next frame")
          _ <- frameCoordinator.signalReady(SignalFrom.Render)
          _ <- Performance.timeframe(frameDuration("Window"), for {
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
          } yield ())
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
