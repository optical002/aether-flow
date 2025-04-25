package engine.os.powershell

import engine.core.Logger
import engine.os.*
import engine.os.executors.*
import zio.*

trait PowershellAppScript extends ShellRunnableScript[PowershellScript] {

  def appName: String
  def appCommandName: String
  
  override protected def executor: ShellExecutor[PowershellScript] = PowershellExecutor

  def runScript(logger: Logger): UIO[Either[Throwable, Unit]] = (for {
    _ <- logger.logVerbose("Starting to execute powershell script")
    fiber <- executor.process(script, logger)
    _ <- logger.logVerbose("Waiting for powershell script to finish executing")
    result <- fiber.join
    _ <- logger.logVerbose("Finished executing powershell script")
    scriptLogger = logger.scope("Script")
    _ <- ZIO.foreach(result)(scriptLogger.logVerbose)
  } yield ()).either
}
