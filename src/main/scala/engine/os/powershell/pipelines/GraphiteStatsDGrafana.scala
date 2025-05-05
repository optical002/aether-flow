package engine.os.powershell.pipelines

import engine.core.logger.ASyncLogger
import engine.os.docker_compose.*
import engine.os.powershell.DockerComposePipeline
import zio.*

object GraphiteStatsDGrafana extends DockerComposePipeline(
  GraphiteStatsDGrafanaYaml(
    graphiteUIPort = 50123,
    grafanaPort = 50124,
    graphiteTCP = 50125,
    statsdUDP = 50126
  ), "graphite-statsd-grafana", permanent = true
)

object Test extends ZIOAppDefault {
  val logger = new ASyncLogger("Test")

  val program = for {
    _ <- GraphiteStatsDGrafana.run(logger)
    _ <- logger.logVerbose("Started")
    _ <- ZIO.sleep(1.hour)
    _ <- logger.logVerbose("Stopping")
  } yield ()

  def run = program.provide(
    ASyncLogger.allowAllLayer,
    Scope.default,
  )
}
