package engine.os.powershell.pipelines

import engine.core.Logger
import engine.os.docker_compose.*
import zio.*

object GraphiteStatsDGrafana extends DockerComposePipeline(GraphiteStatsDGrafanaYaml)

object Test extends ZIOAppDefault {
  def run = GraphiteStatsDGrafana.run(new Logger("Test")).provide(Logger.layer)
}
