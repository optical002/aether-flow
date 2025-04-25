package engine.os

import engine.core.Logger
import zio.*

trait ShellExecutor[Script] {
  def process(script: Script, logger: Logger): UIO[Fiber[Throwable, Seq[String]]]
}
