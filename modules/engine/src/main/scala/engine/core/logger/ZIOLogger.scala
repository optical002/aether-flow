package engine.core.logger

import engine.core.ConsoleColor
import zio.*

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZIOLogger(val scope: String) extends Logger[UIO[Unit], ZIOLogger] {
  import ZIOLogger.*

  override def logLevel(msg: String, level: LogLevel): UIO[Unit] =
    innerLog(msg, level, scope)

  private def innerLog(msg: String, level: LogLevel, scope: String): UIO[Unit] = {
    ZIO.logAnnotate(AnnotationKeys.scope, scope) {
      ZIO.logLevel(level)(ZIO.log(msg))
    }
  }

  override def make(scope: String): ZIOLogger = new ZIOLogger(scope)
  
  // Should get only invoked right before synchronous code to get the right fiber id.
  def toSyncLogger: UIO[SingleFiberConsoleLogger] = 
    ZIO.fiberId.map(fiberId => new SingleFiberConsoleLogger(scope, fiberId))
}
object ZIOLogger {
  object AnnotationKeys {
    val scope = "scope"
  }

  val logger: ZLogger[String, Unit] = (
    trace: Trace, fiberId: FiberId, logLevel: LogLevel, message: () => String, cause: Cause[Any],
    context: FiberRefs, spans: List[LogSpan], annotations: Map[String, String]
  ) => {
    val scope = annotations.get(AnnotationKeys.scope) match {
      case Some(value) => value
      case None        => ""
    }

    println(
     Logger.formatMessage(message(), logLevel = logLevel, fiberId = fiberId, scopeRaw = scope)
    )
  }

  val layer = Runtime.removeDefaultLoggers >>> Runtime.addLogger(logger)
}
