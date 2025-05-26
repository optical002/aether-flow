package aetherflow.game

import aetherflow.engine.components.*
import aetherflow.engine.{App, ecs}
import aetherflow.engine.core.logger.{ASyncLogger, LogFilter}
import aetherflow.engine.ecs.{Component, EcsStateMachine, WorldBuilder}
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.*
import aetherflow.engine.graphics.data.{Mesh, Shader, Vec3f}
import aetherflow.engine.graphics.*
import zio.*

/**
 * Game app for testing game engine locally without any other repositories/projects.
 */
object TestGame extends App {
  override lazy val configs: WindowConfig = new WindowConfig {
    val title = "Game from Scala FP"
    val width = 1980
    val height = 1080
    val frameRate = 60
  }
  override def createLogFilter: LogFilter = new LogFilter(
    allowedLogLevel = LogLevel.Debug,
    customScopeRules = Map(
      "StartUpSystem" -> LogLevel.All,
      "Performance.MonitorWindow" -> LogLevel.None,
      "Performance.MonitorWindow.Metrics" -> LogLevel.None,
    )
  )

  override def graphicsAPI(): GraphicsAPI = opengl.API

  override def startupWorld(
    builder: WorldBuilder
  ): WorldBuilder = builder
    .addStartUpSystem(StartUpSystem)
    .addSystems(0, MovementSystem)
}

object StartUpSystem extends ecs.System {
  override def run(ecsStateMachine: EcsStateMachine, logger: ASyncLogger): Task[Unit] = {
    ecsStateMachine.createEntity(
      Transform(Vec3f.zero, Vec3f.zero, Vec3f.one),
      Renderer.uninitialized(Mesh.box, Shader.standardSource)
    ) *>
    logger.logDebug("Created entity")
  }
}
object MovementSystem extends ecs.System {
  override def run(ecsStateMachine: EcsStateMachine, logger: ASyncLogger): Task[Unit] = for {
    result <- ecsStateMachine.query1[Transform]
    _ <- ZIO.foreach(result) { case (_, transformRef) =>
      for {
        _ <- transformRef.update(_.applyVelocity(Vec3f.one * 0.001f)).commit
      } yield ()
    }
  } yield ()
}
