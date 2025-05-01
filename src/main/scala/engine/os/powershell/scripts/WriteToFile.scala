package engine.os.powershell.scripts

import engine.os.powershell.*
import java.nio.file.Path

class WriteToFile(content: String, path: Path) extends PowershellRunScript {
  override def appName: String = "Write to File"
  override def appCommandName: String = "powershell"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |$$content = @"
         |$content
         |"@
         |$$content | Set-Content -Path ${path.toAbsolutePath}
         |""".stripMargin
  }
}
