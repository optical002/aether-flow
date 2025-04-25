package engine.os

import engine.core.Logger
import zio.*

trait ShellRunnableScript[Script] {
  lazy val scriptName = this.getClass.getSimpleName

  def run(logger: Logger): Task[Boolean] = runInner(logger.scope(s"Script-$scriptName"))
  
  protected def runInner(logger: Logger): Task[Boolean]
  protected def executor: ShellExecutor[Script]

  def script: Script
}
