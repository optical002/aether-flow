package engine.ecs

import engine.core.logger.ASyncLogger
import zio.*

trait System {
  lazy val systemName = this.getClass.getSimpleName.stripSuffix("$")
  
  def run(
    world: World, logger: ASyncLogger
  ): Task[Unit]
}
