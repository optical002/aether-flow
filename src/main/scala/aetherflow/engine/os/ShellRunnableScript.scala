package aetherflow.engine.os

import aetherflow.engine.core.logger.ASyncLogger
import zio.*

trait ShellRunnableScript[Script] {
  lazy val scriptName = this.getClass.getSimpleName

  def run(logger: ASyncLogger): Task[Boolean] = runInner(logger.scope(s"Script-$scriptName"))
  
  protected def runInner(logger: ASyncLogger): Task[Boolean]
  protected def executor: ShellExecutor[Script]

  def script: Script
}
