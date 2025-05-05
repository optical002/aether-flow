package engine.ecs

import engine.core.FrameCoordinator
import engine.core.FrameCoordinator.SignalFrom.ECS
import engine.*
import engine.core.logger.ASyncLogger
import engine.performance.Performance
import engine.performance.PerformanceMetrics.*
import zio.*

import scala.collection.mutable

class WorldBuilder {
  private val startUpSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val priorityUpdateSystems: mutable.Map[Int, mutable.Buffer[ecs.System]] = 
    mutable.Map[Int, mutable.Buffer[ecs.System]]()
  private val logger = new ASyncLogger("ECS-World")

  def addStartUpSystem(system: ecs.System): WorldBuilder = {
    startUpSystems += system
    this
  }

  def addSystems(priority: Int, systems: ecs.System*): WorldBuilder = {
    priorityUpdateSystems.get(priority) match {
      case Some(buffer) => 
        buffer.addAll(systems)
      case None => {
        val newBuffer = mutable.Buffer[ecs.System]()
        newBuffer.addAll(systems)
        priorityUpdateSystems.addOne((priority, newBuffer))
      }
    }
    
    this
  }

  def launchWorld = {
    for {
      world <- World.create
      frameCoordinator <- ZIO.service[FrameCoordinator]
      _ <- logger.logVerbose("Initializing 'startUpSystems'")
      _ <- Performance.measureLabel(ecsStartup,
        ZIO.foreachPar(startUpSystems.toList){ s => 
          Performance.measureLabel(ecsSystemStartup(s.systemName),
            s.run(world, new ASyncLogger(s.systemName))
          )
        }
      )
      prioritizedSystems <- ZIO.succeed(priorityUpdateSystems.toSeq.sortBy(_._1).map(_._2))
      _ <- logger.logVerbose("'startUpSystems' initialized")
      _ <- (for {
        _ <- logger.logVerbose("Waiting for next frame")
        _ <- frameCoordinator.signalReady(ECS)
        _ <- logger.logVerbose("Running 'updateSystems'")
        _ <- Performance.timeframe(frameDuration("ECS"),
          ZIO.foreach(prioritizedSystems) { systems =>
            ZIO.foreachPar(systems) { s =>
              Performance.timeframe(ecsMetric(s.systemName),
                s.run(world, new ASyncLogger(s.systemName))
              )
            }
          }
        )
        _ <- logger.logVerbose("Finished running 'updateSystems'")
      } yield ()).forever
    } yield ()
  }
}
