package engine.resources

import engine.core.{FrameCoordinator, Logger}
import engine.core.FrameCoordinator.SignalFrom
import zio.*

class Time(
  startedAtNsRef: Ref[Long],
  framesPassedRef: Ref[Long],
  clock: Clock,
  frameCoordinator: FrameCoordinator,
) {
  private val logger = new Logger("Time")
  
  def startCounting = for {
    startedAtNs <- clock.nanoTime
    _ <- startedAtNsRef.update(_ => startedAtNs)
  } yield ()
  
  // TODO frame delta time.
  
  val millisSinceStart = for {
    startedAtNs <- startedAtNsRef.get
    now <- clock.nanoTime
  } yield (now - startedAtNs) / 1_000_000

  def run = {
    def loop(): UIO[Unit] = for {
      _ <- logger.logVerbose("Updating frame count by 1")
      // At the end of the frame update, since we start from -1 frame, first frame it will be 0
      _ <- framesPassedRef.update(_ + 1)
      _ <- logger.logVerbose("Waiting for next frame")
      _ <- frameCoordinator.signalReady(SignalFrom.Time)
      _ <- loop()
    } yield ()
    
    loop()
  }.fork
}
object Time {
  val layer = ZLayer.fromZIO { for {
    startedAtNsRef <- Ref.make[Long](0)
    framesPassedRef <- Ref.make[Long](-1)
    clock <- ZIO.clock
    frameCoordinator <- ZIO.service[FrameCoordinator]
  } yield new Time(startedAtNsRef, framesPassedRef, clock, frameCoordinator)}
}
