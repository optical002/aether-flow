package engine.ecs

import engine.core.Logger
import zio.*

trait System {
  def run(
    world: World, logger: Logger
  ): Task[Unit]
}
