package engine.core.logger

import zio.*

class LogFilter(
  allowedLogLevel: LogLevel, customScopeRules: Map[String, LogLevel]
) {
  def shouldPrint(scope: String, logLevel: LogLevel): Boolean = {
    checkLogLevel(trying = logLevel, allowed = customScopeRules.find { case (s, _) => s.contains(scope) } match {
      case Some((_, scopeAllowedLogLevel)) => scopeAllowedLogLevel
      case None => allowedLogLevel
    })
  }

  private def checkLogLevel(trying: LogLevel, allowed: LogLevel): Boolean = {
    val sortedByPriority = LogLevel.levels.toList.sorted
    val tryingIndex = sortedByPriority.indexOf(trying)
    val allowedIndex = sortedByPriority.indexOf(allowed)
    tryingIndex >= allowedIndex
  }
}
object LogFilter {
  val allowAll = new LogFilter(LogLevel.All, Map.empty)
}
