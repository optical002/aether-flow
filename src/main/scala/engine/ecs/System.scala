package engine.ecs

import engine.core.logger.ZIOLogger
import zio.*

trait System {
  lazy val systemName = this.getClass.getSimpleName.stripSuffix("$")
  
  def run(
    world: World, logger: ZIOLogger
  ): Task[Unit]
}
