package aetherflow.engine.core.logger

trait LogMessage[A] {
  def asString(a: A): String
}
object LogMessage {
  given LogMessage[String] = str => str
  given LogMessage[Boolean] = b => b.toString
  given LogMessage[Throwable] = t => s"${t.getMessage}: \n${t.getStackTrace.mkString("\n")}"
}
