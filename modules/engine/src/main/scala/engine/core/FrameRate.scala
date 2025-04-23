package engine.core

import engine.core.FrameCoordinator.SignalFrom.FrameRate
import zio.*

class FrameRate(
  frameRate: Int,
  frameCoordinator: FrameCoordinator
){
  def run = (for {
    _ <- frameCoordinator.signalReady(FrameCoordinator.SignalFrom.FrameRate)
    _ <- ZIO.sleep((1_000_000_000 / frameRate).nanoseconds)
  } yield ()).forever.fork
}
object FrameRate {
  def layer(frameRate: Int) = ZLayer.fromZIO(for{
    frameCoordinator <- ZIO.service[FrameCoordinator]
  } yield new FrameRate(frameRate, frameCoordinator))
}
