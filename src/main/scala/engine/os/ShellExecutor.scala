package engine.os

import engine.core.logger.ASyncLogger
import zio.*

trait ShellExecutor[Script] {
  def process(script: Script, logger: ASyncLogger): UIO[Fiber[Throwable, Seq[String]]]
}
