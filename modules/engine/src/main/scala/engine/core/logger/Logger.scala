package engine.core.logger

import engine.core.ConsoleColor
import engine.core.ConsoleColor.*
import zio.*

import java.io.{OutputStream, PrintStream}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

trait Logger[+Out, Self <: Logger[Out, Self]] {
  def scope: String
  def logLevel[A: LogMessage](msg: A, level: LogLevel): Out
  def make(scope: String): Self

  def logVerbose[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.All)
  def logInfo[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.Info)
  def logDebug[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.Debug)
  def logWarning[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.Warning)
  def logError[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.Error)
  def logFatal[A: LogMessage](msg: A): Out = logLevel(msg, LogLevel.Fatal)

  def scope(innerScope: String): Self = make(s"$scope.$innerScope")
}
object Logger {
  def formatMessage(
    msg: String, logLevel: LogLevel, fiberId: FiberId, scopeRaw: String
  ): String = {
    import ConsoleColor.*

    val timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    val scope = s"[$scopeRaw]"
    val logLevelColor = logLevel match {
      case LogLevel.Fatal   => RED
      case LogLevel.Error   => RED
      case LogLevel.Warning => YELLOW
      case LogLevel.Info    => CYAN
      case LogLevel.Debug   => GREEN
      case LogLevel.Trace   => GRAY
      case LogLevel.All     => RESET
      case _                => RESET
    }

    val threadText = s"[${fiberId.threadName}]"
    val timeText = s"[$timestamp]"
    val logLevelText = s"$logLevelColor[${logLevel.label}]"
    f"$GRAY$threadText%-25s$timeText%15s$logLevelText%-10s $scope%25s: ${msg}$RESET"
  }
}
