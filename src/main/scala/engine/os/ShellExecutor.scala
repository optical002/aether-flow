package engine.os

import engine.core.logger.ZIOLogger
import zio.*

trait ShellExecutor[Script] {
  def process(script: Script, logger: ZIOLogger): UIO[Fiber[Throwable, Seq[String]]]
}
