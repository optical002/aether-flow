package engine.core

import zio.*

object FrameCoordinator {
  type Barrier = UIO[Unit]

  def makeBarrier(parties: Int): UIO[Barrier] = for {
    counter <- Ref.make(0)
    initialPromise <- Promise.make[Nothing, Unit]
    promiseRef <- Ref.make[Promise[Nothing, Unit]](initialPromise)
  } yield {
    def await: UIO[Unit] = for {
      latch <- promiseRef.get
      n <- counter.updateAndGet(_ + 1)
      _ <- if (n == parties) for {
        newLatch <- Promise.make[Nothing, Unit]
        _ <- promiseRef.set(newLatch)
        _ <- counter.set(0)
        _ <- latch.succeed(())
      } yield ()
      else latch.await

    } yield ()

    await
  }
}
