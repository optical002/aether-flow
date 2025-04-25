package engine.os.executors

import zio.*
import engine.core.Logger

trait ShellExecutor[Script] {
  def process(script: Script, logger: Logger): UIO[Fiber[Throwable, Seq[String]]]
}
