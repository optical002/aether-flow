package engine.os.powershell.scripts

import engine.os.powershell.{PowershellRunScript, PowershellScript}

import java.nio.file.Path


class DeleteFile(file: Path) extends PowershellRunScript {
  override def appName: String = "Write to File"
  override def appCommandName: String = "powershell"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |Remove-Item -Path "${file.toAbsolutePath}" -Force -ErrorAction SilentlyContinue
         |""".stripMargin
  }
}
