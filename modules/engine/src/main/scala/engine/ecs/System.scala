package engine.ecs

import zio.*

trait System {
  def run(world: World): Task[Unit]
}
