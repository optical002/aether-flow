package aetherflow.engine.core.logger

import zio.*

class LogFilter(
  allowedLogLevel: LogLevel, customScopeRules: Map[String, LogLevel]
) {
  def shouldPrint(scope: String, logLevel: LogLevel): Boolean = {
    checkLogLevel(
      trying = logLevel, 
      allowed = customScopeRules
        .toList
        .sortBy { case (s, _) => -s.length } // prioritize more specific scopes
        .find { case (s, _) => scope.contains(s) }
        .map(_._2)
        .getOrElse(allowedLogLevel)
    )
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
