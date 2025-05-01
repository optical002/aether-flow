package engine

import engine.core.logger.ZIOLogger
import engine.os.powershell.scripts.TestScript
import zio.*

object BuildApp extends ZIOAppDefault {
  def run = (new TestScript).runScript(new ZIOLogger("Test")).provide(
    ZIOLogger.allowAllLayer
  )
}
