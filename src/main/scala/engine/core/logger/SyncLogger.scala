package engine.core.logger

import zio.*

/** Logger for non-ZIO, synchronous code running on a single fiber. */
class SyncLogger(
  val scope: String, fiberId: FiberId, logFilter: LogFilter
) extends Logger[Unit, SyncLogger] {
  override def logLevel[A: LogMessage](msg: A, level: LogLevel): Unit = {
    if (logFilter.shouldPrint(scope, level)) {
      println(Logger.formatMessage(
        summon[LogMessage[A]].asString(msg), logLevel = level, fiberId = fiberId, scopeRaw = scope
      ))
    }
  }
  
  override def make(scope: String): SyncLogger = new SyncLogger(scope, fiberId, logFilter)
}
