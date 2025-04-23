package engine.core

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
    frameRate: Gate,
    time: Gate
  ) {
    def updateReady(signalFrom: SignalFrom): Gates = signalFrom match {
      case SignalFrom.Render    => copy(render = true)
      case SignalFrom.ECS       => copy(ecs = true)
      case SignalFrom.FrameRate => copy(frameRate = true)
      case SignalFrom.Time      => copy(time = true)
    }

    def allowPassage: Boolean = ecs && render && frameRate
  }
  object Gates {
    lazy val empty = Gates(false, false, false, false)
  }

  enum SignalFrom {
    case Render
    case ECS
    case FrameRate
    case Time
  }

  val layer = ZLayer(for {
    gatesRef <- Ref.make(Gates.empty)
    initialPromise <- Promise.make[Nothing, Unit]
    latchRef <- Ref.make(initialPromise)
  } yield new FrameCoordinator(gatesRef = gatesRef, latchRef = latchRef))
}
