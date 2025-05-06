package aetherflow.engine.core

import zio.*

class FrameCoordinator(
  gatesRef: Ref[FrameCoordinator.Gates],
  latchRef: Ref[Promise[Nothing, Unit]],
) {
  def signalReady(signalFrom: FrameCoordinator.SignalFrom): FrameCoordinator.Barrier = for {
    latch <- latchRef.get
    gates <- gatesRef.updateAndGet(_.updateReady(signalFrom))
    _ <- if (gates.allowPassage) for {
      newLatch <- Promise.make[Nothing, Unit]
      _ <- latchRef.set(newLatch)
      _ <- gatesRef.set(FrameCoordinator.Gates.empty)
      _ <- latch.succeed(())
    } yield ()
    else latch.await
  } yield ()
}
object FrameCoordinator {
  type Barrier = UIO[Unit]
  type Gate = Boolean

  case class Gates(
    ecs: Gate,
    render: Gate,
    frameLimiter: Gate,
    time: Gate,
    frameDurationPerformance: Gate
  ) {
    def updateReady(signalFrom: SignalFrom): Gates = signalFrom match {
      case SignalFrom.Render => copy(render = true)
      case SignalFrom.ECS => copy(ecs = true)
      case SignalFrom.FrameLimiter => copy(frameLimiter = true)
      case SignalFrom.FrameDurationPerformance => copy(frameDurationPerformance = true)
      case SignalFrom.Time => copy(time = true)
    }

    def allowPassage: Boolean = ecs && render && frameLimiter
  }
  object Gates {
    lazy val empty = Gates(false, false, false, false, false)
  }

  enum SignalFrom {
    case Render
    case ECS
    case FrameLimiter
    case FrameDurationPerformance
    case Time
  }

  val layer = ZLayer(for {
    gatesRef <- Ref.make(Gates.empty)
    initialPromise <- Promise.make[Nothing, Unit]
    latchRef <- Ref.make(initialPromise)
  } yield new FrameCoordinator(gatesRef = gatesRef, latchRef = latchRef))
}
