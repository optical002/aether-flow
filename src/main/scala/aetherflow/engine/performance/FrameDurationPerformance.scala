package aetherflow.engine.performance

import aetherflow.engine.core.FrameCoordinator
import aetherflow.engine.core.FrameCoordinator.*
import zio.*

object FrameDurationPerformance {
  def run = for {
    frameCoordinator <- ZIO.service[FrameCoordinator]
    fiber <- (for {
      _ <- Performance.measureLabel(PerformanceMetrics.frameDurationPerformance,
        frameCoordinator.signalReady(SignalFrom.FrameDurationPerformance)
      )
    } yield ()).forever.fork
  } yield fiber
}
