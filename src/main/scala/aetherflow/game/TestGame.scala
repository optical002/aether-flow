package aetherflow.game

import aetherflow.engine.components.*
import aetherflow.engine.{App, ecs}
import aetherflow.engine.core.logger.{ASyncLogger, LogFilter}
import aetherflow.engine.ecs.{Component, EcsStateMachine, WorldBuilder}
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.*
import aetherflow.engine.graphics.data.{MeshLegacy, Shader, Vec3f}
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
    def createBox(
      position: Vec3f = Vec3f.zero,
      rotation: Vec3f = Vec3f.zero,
      scale: Vec3f = Vec3f.one
    ): UIO[Unit] = {
      ecsStateMachine.createEntity(
        Transform(position, Vec3f.zero, Vec3f.one),
        Renderer.uninitialized(MeshLegacy.box, Shader.standardSource)
      ).unit
    }

    createBox() *>
    createBox(position = Vec3f(2, 3, 1)) *>
    createBox(position = Vec3f(-2, 3, 1)) *>
    createBox(position = Vec3f(0, -3, 1)) *>
    createBox(position = Vec3f(2, -3, 1)) *>
    createBox(position = Vec3f(-2, -3, 1)) *>
    createBox(position = Vec3f(0, 0, 1))
  }
}
object MovementSystem extends ecs.System {
  override def run(ecsStateMachine: EcsStateMachine, logger: ASyncLogger): Task[Unit] = for {
    result <- ecsStateMachine.query1[Transform]
    _ <- ZIO.foreach(result) { case (_, transformRef) =>
      for {
        _ <- transformRef.update(_.applyVelocity(Vec3f.one * 0.005f)).commit
      } yield ()
    }
  } yield ()
}
