package engine.core

import zio.*
import zio.logging.*
import zio.config.typesafe.TypesafeConfigProvider
import zio.logging.consoleLogger
import zio.logging.LogFormat.*

import java.io.{FileWriter, PrintWriter}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Logger(scope: String) {
  def logLevel(msg: String, level: LogLevel): UIO[Unit] = {
    ZIO.logAnnotate("scope", scope) {
      ZIO.logLevel(level)(ZIO.log(msg))
    }
  }

  def logVerbose(msg: String): UIO[Unit] = logLevel(msg, LogLevel.All)
  def logInfo(msg: String): UIO[Unit] = logLevel(msg, LogLevel.Info)
  def logError(msg: String): UIO[Unit] = logLevel(msg, LogLevel.Error)
  def logFatal(msg: String): UIO[Unit] = logLevel(msg, LogLevel.Fatal)
}
object Logger {
  val logger: ZLogger[String, Unit] = (
    trace: Trace, fiberId: FiberId, logLevel: LogLevel, message: () => String, cause: Cause[Any],
    context: FiberRefs, spans: List[LogSpan], annotations: Map[String, String]
  ) => {
    val timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    val scope = annotations.get("scope") match {
      case Some(value) => s"[$value]"
      case None        => ""
    }
    import ConsoleColor.*
    val logLevelColor = logLevel match {
      case LogLevel.Fatal   => RED
      case LogLevel.Error   => RED
      case LogLevel.Warning => YELLOW
      case LogLevel.Info    => CYAN
      case LogLevel.Debug   => GREEN
      case LogLevel.Trace   => GRAY
      case _                => RESET
    }

    val threadText = s"[${fiberId.threadName}]"
    val timeText = s"[$timestamp]"
    val logLevelText = s"$logLevelColor[${logLevel.label}]"
    val formated =
      f"$GRAY$threadText%-25s$timeText%15s$logLevelText%-10s $scope%25s: ${message()}$RESET"

    println(formated)
  }

  val layer = Runtime.removeDefaultLoggers >>> Runtime.addLogger(logger)
}
