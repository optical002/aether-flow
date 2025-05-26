package aetherflow.engine.ecs

import aetherflow.engine.core.logger.ASyncLogger
import zio.*

trait System {
  lazy val systemName = this.getClass.getSimpleName.stripSuffix("$")
  
  def run(
    ecsStateMachine: EcsStateMachine, logger: ASyncLogger
  ): Task[Unit]
}
