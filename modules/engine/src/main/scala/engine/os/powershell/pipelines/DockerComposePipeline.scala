package engine.os.powershell.pipelines

import engine.os.docker_compose.GraphiteStatsDGrafanaYaml
import engine.os.powershell.scripts.{DockerComposeUp, InstallDocker, InstallDockerCompose}
import engine.os.{DockerComposeYaml, ScriptPipeline, ShellRunnableScript}

class DockerComposePipeline(yaml: DockerComposeYaml) extends ScriptPipeline {
  override def scripts: Seq[ShellRunnableScript[_]] = Seq(
    InstallDocker,
    InstallDockerCompose,
    DockerComposeUp(yaml),
  )
}
