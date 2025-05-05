package game

import engine.*
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.WindowConfig
import engine.graphics.opengl.API
import engine.graphics.opengl.shaders.StandardShader
import engine.core.logger.ASyncLogger

import zio.*

object GameMain extends engine.App {
  override lazy val configs: WindowConfig = new WindowConfig {
    val title = "Game from Scala FP"
    val width = 800
    val height = 600
    val frameRate = 60
  }

  override def graphicsAPI(): GraphicsAPI = API

  override def render(db: GraphicDatabase): Task[Unit] = for {
    _ <- db.load(GraphicAsset(VertexData.Quad, StandardShader))
  } yield ()

  override def startupWorld(
    builder: WorldBuilder
  ): WorldBuilder = builder
    .addStartUpSystem(StartUpSystem)
    .addSystems(priority = 0, MovementSystem)
    .addSystems(priority = 1, LogSystem)

  override def enableMetrics: Boolean = false
}

object StartUpSystem extends ecs.System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] =
    world.createEntity(Transform(0, 0), Velocity(1, 1))
      *> logger.logDebug("Created entity")
}
object LogSystem extends ecs.System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] = for {
    result <- world.query2[Transform, Velocity]
    strings <- ZIO.foreach(result) { case (e, transformRef, velocityRef) =>
      for {
        transform <- transformRef.get.commit
        velocity <- velocityRef.get.commit
      } yield s"Entity($e), $transform, $velocity"
    }
    _ <- logger.logDebug(
      s"Entities queried: ${result.length}, [\n  ${strings.mkString(",\n  ")}\n]"
    )
  } yield ()
}
object MovementSystem extends ecs.System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] = for {
    result <- world.query2[Transform, Velocity]
    _ <- ZIO.foreach(result) { case (_, transformRef, velocityRef) =>
      for {
        velocity <- velocityRef.get.commit
        _ <- transformRef.update(_.applyVelocity(velocity)).commit
        _ <- logger.logDebug("Moved transform by velocity")
      } yield ()
    }
  } yield ()
}
