package engine.os

import engine.core.logger.ZIOLogger
import zio.*

trait ShellRunnableScript[Script] {
  lazy val scriptName = this.getClass.getSimpleName

  def run(logger: ZIOLogger): Task[Boolean] = runInner(logger.scope(s"Script-$scriptName"))
  
  protected def runInner(logger: ZIOLogger): Task[Boolean]
  protected def executor: ShellExecutor[Script]

  def script: Script
}
