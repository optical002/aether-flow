package engine.os.powershell

import engine.core.logger.ZIOLogger
import zio.*

trait PowershellRunScript extends PowershellAppScript {
  import engine.os.Utils.*

  override protected def runInner(logger: ZIOLogger): Task[Boolean] = for {
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
