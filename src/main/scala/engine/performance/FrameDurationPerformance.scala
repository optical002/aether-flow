package engine.performance

import engine.core.FrameCoordinator
import engine.core.FrameCoordinator.SignalFrom
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
