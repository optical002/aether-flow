package engine.resources

import engine.core.FrameCoordinator
import engine.core.FrameCoordinator.SignalFrom
import engine.core.logger.ZIOLogger
import engine.performance.Performance
import engine.performance.PerformanceMetrics.*
import zio.*

class Time(
  startedAtNsRef: Ref[Long],
  framesPassedRef: Ref[Long],
  clock: Clock,
  frameCoordinator: FrameCoordinator,
) {
  private val logger = new ZIOLogger("Time")
  
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
      _ <- Performance.timeframe(frameDuration("Time"), for {
        _ <- logger.logVerbose("Updating frame count by 1")
        // At the end of the frame update, since we start from -1 frame, first frame it will be 0
        framesPassed <- framesPassedRef.updateAndGet(_ + 1)
        millisPassed <- millisSinceStart
        _ <- logger.logVerbose("Sending fps")
        _ <- ZIO.succeed(framesPassed.toDouble / (millisPassed / 1000)) @@ Performance.labelUnit(fps)
        _ <- logger.logVerbose("Waiting for next frame")
      } yield ())
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
