package aetherflow.engine.performance

import aetherflow.engine.core.FrameCoordinator
import aetherflow.engine.core.FrameCoordinator.*
import aetherflow.engine.core.logger.ASyncLogger
import zio.*

object FrameDuration {
  private val logger = new ASyncLogger("Frame-Duration-Performance")
  
  def run = for {
    _ <- logger.logVerbose("Starting")
    frameCoordinator <- ZIO.service[FrameCoordinator]
    fiber <- (for {
      _ <- API.measureLabel(Metrics.frameDurationPerformance,
        frameCoordinator.signalReady(SignalFrom.FrameDurationPerformance)
      )
    } yield ()).forever.onDone(
      error = _ => logger.logVerbose("Closing with error"),
      success = _ => logger.logVerbose("Closing")
    ).fork
  } yield fiber
}
