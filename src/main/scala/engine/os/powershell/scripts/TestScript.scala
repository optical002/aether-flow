package engine.os.powershell.scripts

import engine.os.powershell.{PowershellRunScript, PowershellScript}

import java.nio.file.Path

class TestScript extends PowershellRunScript {
  override def appName: String = "Write to File"
  override def appCommandName: String = "powershell"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |echo "Hello World"
         |""".stripMargin
  }
}
