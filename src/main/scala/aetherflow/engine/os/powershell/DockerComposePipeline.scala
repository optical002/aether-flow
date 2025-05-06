package aetherflow.engine.os.powershell

import aetherflow.engine.os.{DockerComposeYaml, ScriptPipeline}
import aetherflow.engine.os.powershell.scripts.{DeleteFile, DockerComposeDown, DockerComposeStop, DockerComposeUp, InstallDocker, InstallDockerCompose, WriteToFile}
import zio.*

import java.nio.file.*

class DockerComposePipeline(
  yaml: DockerComposeYaml, name: String, permanent: Boolean
) extends ScriptPipeline[Path] {
  override def createData: Task[Path] = for {
    path <- ZIO.attempt(
      if (permanent) {
        val systemDrive = Files.createTempFile("temp-", "").toAbsolutePath.toString.substring(0, 2)
        Paths.get(s"$systemDrive/temp").resolve("docker-compose.yaml")
      } else Files.createTempFile("docker-compose-", ".yml")
    )
    _ <- ZIO.log(path.toAbsolutePath.toString)
  } yield path

  override def startupScripts(data: Path) = Seq(
    InstallDocker,
    InstallDockerCompose,
    WriteToFile(content = yaml.get, path = data),
    DockerComposeUp(yamlFile = data, name = name),
  )

  override def closeScripts(data: Path) = 
    if (permanent) Seq(
      DockerComposeStop(yamlFile = data, name = name)
    )
    else Seq (
      DockerComposeDown(yamlFile = data, name = name),
      DeleteFile(file = data)
    )
}
