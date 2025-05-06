package aetherflow.engine.os

import zio.*

object Utils {
  def isWindowsOs: Task[Boolean] = ZIO.attempt(
    java.lang.System.getProperty("os.name").toLowerCase.contains("win")
  )

  def isCommandAvailable(cmd: String): Task[Boolean] = for {
    isWindows <- isWindowsOs
    result <- ZIO.attempt(
      os.proc(
        if (isWindows) Seq("powershell", "-Command", s"Get-Command $cmd")
        else Seq("which", cmd)
      ).call().exitCode == 0
    ).either.map {
      case Right(result) => result
      case Left(_) => false
    }
  } yield result
}
