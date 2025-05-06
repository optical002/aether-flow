package aetherflow.engine.os.powershell

import aetherflow.engine.core.logger.ASyncLogger
import aetherflow.engine.os.Utils.*
import zio.*

trait PowershellRunScript extends PowershellAppScript {

  override protected def runInner(logger: ASyncLogger): Task[Boolean] = for {
    isAvailable <- isCommandAvailable(appCommandName)
    isWindowsOs <- isWindowsOs
    result <- (isAvailable, isWindowsOs) match {
      case (true, true) =>
        logger.logVerbose(s"Running $scriptName via $appName...")
          *> runScript(logger)
      case _ => ZIO.succeed(Left(new RuntimeException(s"Command not found: $appCommandName")))
    }
    _ <- result match {
      case Right(_) => logger.logVerbose(s"✅ Script $scriptName via $appName ran successfully.")
      case Left(e)  => logger.logFatal(s"❌ Script $scriptName via $appName failed: ${e.getMessage}")
    }
  } yield result.isRight
}
