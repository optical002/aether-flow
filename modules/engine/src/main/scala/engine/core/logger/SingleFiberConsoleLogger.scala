package engine.core.logger

import zio.*

/** Logger for non-ZIO, synchronous code running on a single fiber. */
class SingleFiberConsoleLogger(
  val scope: String, fiberId: FiberId, logFilter: LogFilter
) extends Logger[Unit, SingleFiberConsoleLogger] {
  override def logLevel(msg: String, level: LogLevel): Unit = {
    if (logFilter.shouldPrint(scope, level)) {
      println(Logger.formatMessage(msg, logLevel = level, fiberId = fiberId, scopeRaw = scope))
    }
  }
  
  override def make(scope: String): SingleFiberConsoleLogger = new SingleFiberConsoleLogger(scope, fiberId, logFilter)
}
