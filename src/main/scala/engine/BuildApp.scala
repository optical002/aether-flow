package engine

import engine.core.logger.ASyncLogger
import engine.os.powershell.scripts.TestScript
import zio.*

object BuildApp extends ZIOAppDefault {
  def run = (new TestScript).runScript(new ASyncLogger("Test")).provide(
    ASyncLogger.allowAllLayer
  )
}
