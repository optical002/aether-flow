package aetherflow.engine.ecs

import zio.*

class WorldManager {
  def loadWorld(worldBuilder: WorldBuilder) = worldBuilder.launchWorld.fork
}
object WorldManager {
  val layer = ZLayer.succeed(new WorldManager)
}
