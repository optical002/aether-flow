import engine.core.logger.ZIOLogger
import engine.ecs
import engine.ecs.{Transform, Velocity, World, WorldBuilder}
import engine.graphics.GraphicsAPI
import engine.graphics.config.WindowConfig
import engine.graphics.*
import engine.graphics.opengl.shaders.StandardShader
import zio.*

object GameMain extends engine.App {
  override lazy val configs: WindowConfig = new WindowConfig {
    val title = "Game from Scala FP"
    val width = 800
    val height = 600
    val frameRate = 60
  }

  override def graphicsAPI(): GraphicsAPI = opengl.API

  override def render(db: GraphicDatabase): Task[Unit] = for {
    _ <- db.load(GraphicAsset(VertexData.Quad, StandardShader))
  } yield ()

  override def startupWorld(
    builder: WorldBuilder
  ): WorldBuilder = builder
    .addStartUpSystem(StartUpSystem)
    .addSystem(MovementSystem)
}

object StartUpSystem extends ecs.System {
  override def run(world: World, logger: ZIOLogger): Task[Unit] = ZIO.succeed {
    world.createEntity(Transform(0, 0), Velocity(1, 1))
  } *> logger.logDebug("Created entity")
}
object MovementSystem extends ecs.System {
  override def run(world: World, logger: ZIOLogger): Task[Unit] = for {
    _ <- ZIO.succeed {
      val result = world.query2[Transform, Velocity]
      for ((id, t, v) <- result) {
        val moved = t.copy(x = t.x + v.dx, y = t.y + v.dy)
      }
    }
    _ <- logger.logDebug("Moved entity")
  }yield ()
}
