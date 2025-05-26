package aetherflow.engine.ecs

import aetherflow.engine.core.logger.ASyncLogger
import zio.*

class WorldManager {
  private val logger = new ASyncLogger("World-Manager")
  
  def loadWorld(worldBuilder: WorldBuilder, ecsStateMachine: EcsStateMachine) = {
    logger.logVerbose("Starting") *>
    worldBuilder.launchWorld(ecsStateMachine).onDone(
      error = _ => logger.logVerbose("Closing with error"),
      success = _ => logger.logVerbose("Closing")
    ).fork
  }
}
object WorldManager {
  val layer = ZLayer.succeed(new WorldManager)
}
