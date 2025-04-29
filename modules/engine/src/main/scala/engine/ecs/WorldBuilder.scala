package engine.ecs

import engine.core.FrameCoordinator
import engine.core.FrameCoordinator.SignalFrom.ECS
import engine.*
import engine.core.logger.ZIOLogger
import engine.performance.Performance
import engine.performance.PerformanceMetrics.*
import zio.*

import scala.collection.mutable

class WorldBuilder {
  private val startUpSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val updateSystems: mutable.Buffer[ecs.System] = mutable.Buffer[ecs.System]()
  private val logger = new ZIOLogger("ECS-World")

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
      _ <- Performance.measureLabel(ecsStartup,
        ZIO.foreachPar(startUpSystems.toList){ s => 
          Performance.measureLabel(ecsSystemStartup(s.systemName),
            s.run(world, new ZIOLogger(s.systemName))
          )
        }
      )
      _ <- logger.logVerbose("'startUpSystems' initialized")
      _ <- (for {
        _ <- logger.logVerbose("Waiting for next frame")
        _ <- frameCoordinator.signalReady(ECS)
        _ <- logger.logVerbose("Running 'updateSystems'")
        _ <- Performance.timeframe(frameDuration("ECS"),
          ZIO.foreachPar(updateSystems.toList) { s =>
            Performance.timeframe(ecsMetric(s.systemName),
              s.run(world, new ZIOLogger(s.getClass.getName))
            )
          }
        )
        _ <- logger.logVerbose("Finished running 'updateSystems'")
      } yield ()).forever
    } yield ()
  }
}
