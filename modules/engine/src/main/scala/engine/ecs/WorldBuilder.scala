package engine.ecs

import engine.core.FrameCoordinator
import engine.core.FrameCoordinator.SignalFrom.ECS
import engine.*
import zio.*

import scala.collection.mutable

class WorldBuilder {
  private val startUpSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val updateSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()

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
      _ <- ZIO.foreachPar(startUpSystems.toList)(_.run(world))
      _ <- (for {
        _ <- frameCoordinator.signalReady(ECS)
        _ <- ZIO.foreachPar(updateSystems.toList)(_.run(world))
      } yield ()).forever
    } yield ()
  }
}
