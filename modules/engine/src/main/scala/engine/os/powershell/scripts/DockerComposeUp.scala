package engine.os.powershell.scripts

import engine.os.*
import engine.os.powershell.*

class DockerComposeUp(yaml: DockerComposeYaml) extends PowershellRunScript {
  override def appName: String = "Docker Compose"
  override def appCommandName: String = "docker-compose"

  override def script: PowershellScript = new PowershellScript {
    def get: String =
      s"""
         |# Create the docker-compose.yml file
         |$$yamlContent = @"
         |${yaml.get}
         |"@
         |$$yamlContent | Set-Content -Path "C:\\temp\\docker-compose.yml"
         |
         |# Run docker-compose up with the provided YAML
         |docker-compose -f "C:\\temp\\docker-compose.yml" up -d
         |
         |# Optionally, verify the services are running
         |docker-compose -f "C:\\temp\\docker-compose.yml" ps
         |""".stripMargin
  }
}
