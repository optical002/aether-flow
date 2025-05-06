package aetherflow.engine.os.powershell

import aetherflow.engine.core.logger.ASyncLogger
import aetherflow.engine.os.Utils.*
import zio.*

trait PowershellInstallScript extends PowershellAppScript {
  
  override protected def runInner(logger: ASyncLogger): Task[Boolean] = for {
    isAvailable <- isCommandAvailable(appCommandName)
    isWindowsOs <- isWindowsOs
    result <- (isAvailable, isWindowsOs) match {
      case (true, _) =>
        logger.logVerbose(s"✅ $appName is already installed.")
          *> ZIO.succeed(Right(()))
      case (false, true) =>
        logger.logVerbose(s"Installing $appName on Windows...")
          *> runScript(logger)
      case _ => ZIO.succeed(Left(new RuntimeException("Unsupported OS or package manager")))
    }
    _ <- result match {
      case Right(_) => logger.logVerbose(s"✅ $appName installation completed.")
      case Left(e)  => logger.logFatal(s"❌ $appName installation failed: ${e.getMessage}")
    }
  } yield result.isRight
}
