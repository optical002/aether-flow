package engine.core.logger

import zio.*

import java.io.PrintStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ASyncLogger(
  val scope: String
) extends Logger[UIO[Unit], ASyncLogger] {
  import ASyncLogger.*

  override def logLevel[A: LogMessage](msg: A, level: LogLevel): UIO[Unit] =
    ZIO.logAnnotate(AnnotationKeys.scope, scope) {
      ZIO.logLevel(level)(ZIO.log(summon[LogMessage[A]].asString(msg)))
    }

  override def make(scope: String): ASyncLogger = new ASyncLogger(scope)
  
  // Should get only invoked right before synchronous code to get the right fiber id.
  def toSyncLogger: URIO[LogFilter, SyncLogger] = for {
    fiberId <- ZIO.fiberId
    logFilter <- ZIO.service[LogFilter]
  } yield new SyncLogger(scope, fiberId, logFilter)
}
object ASyncLogger {
  object AnnotationKeys {
    val scope = "scope"
  }

  // TODO redirect logs to here.
  def logger(
    logFilter: LogFilter
  ): ZLogger[String, Unit] = (
    trace: Trace, fiberId: FiberId, logLevel: LogLevel, message: () => String, cause: Cause[Any],
    context: FiberRefs, spans: List[LogSpan], annotations: Map[String, String]
  ) => {
    val scope = annotations.get(AnnotationKeys.scope) match {
      case Some(value) => value
      case None        => ""
    }

    if (logFilter.shouldPrint(scope, logLevel)) {
      println(
        Logger.formatMessage(message(), logLevel = logLevel, fiberId = fiberId, scopeRaw = scope)
      )
    }
  }

  val allowAllLayer = ZLayer.succeed(LogFilter.allowAll) >>> layer
  val layer = for {
    logFilter <- ZLayer.service[LogFilter]
    _ <- Runtime.removeDefaultLoggers
    any <- Runtime.addLogger(logger(logFilter.get))
  } yield any
}
