package engine.ecs

import engine.core.{FrameCoordinator, Logger}
import engine.core.FrameCoordinator.SignalFrom.ECS
import engine.*
import zio.*

import scala.collection.mutable

class WorldBuilder {
  private val startUpSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val updateSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val logger = new Logger("ECS-World")

  def addStartUpSystem(system: ecs.System): WorldBuilder = {
    startUpSystems += system
    this
  }

  def addSystem(system: ecs.System): WorldBuilder = {
    updateSystems += system
    this
  }

  def launchWorld = {
    val world = new World

    for {
      frameCoordinator <- ZIO.service[FrameCoordinator]
      _ <- logger.logVerbose("Initializing 'startUpSystems'")
      _ <- ZIO.foreachPar(startUpSystems.toList)(s => s.run(world, new Logger(s.getClass.getName)))
      _ <- logger.logVerbose("'startUpSystems' initialized")
      _ <- (for {
        _ <- logger.logVerbose("Waiting for next frame")
        _ <- frameCoordinator.signalReady(ECS)
        _ <- logger.logVerbose("Running 'updateSystems'")
        _ <- ZIO.foreachPar(updateSystems.toList)(s => s.run(world, new Logger(s.getClass.getName)))
        _ <- logger.logVerbose("Finished running 'updateSystems'")
      } yield ()).forever
    } yield ()
  }
}
