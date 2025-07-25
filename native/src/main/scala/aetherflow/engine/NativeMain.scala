package aetherflow.engine

import kyo.*

object NativeMain extends KyoApp {
  run {
    for {
      _ <- Console.printLine(s"[${Thread.currentThread().getName}] Hello scala native from kyo")
      _ <- Async.sleep(1.second)
      _ <- Console.printLine(s"[${Thread.currentThread().getName}] Hello scala native from kyo")
      _ <- Async.sleep(1.second)
      _ <- Console.printLine(s"[${Thread.currentThread().getName}] Hello scala native from kyo")
    } yield ()
  }
}
