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
    .addSystem(MovementSystem)

  override def enableMetrics: Boolean = false
}

case class TestComponent(a: Int) extends Component

object StartUpSystem extends ecs.System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] = 
    world.createEntity(Transform(0, 0), Velocity(1, 1), TestComponent(1)) 
      *> logger.logDebug("Created entity")
}
object MovementSystem extends ecs.System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] = for {
    // TODO write tests and try to break it via concurrency
    result1 <- world.query2[Transform, Velocity]
    result2 <- world.query2[Transform, TestComponent]
    result3 <- world.query3[Transform, TestComponent, Velocity]
    _ <- logger.logDebug(s"Entities queried1: ${result1.length}, Entities queried2: ${result2.length}, Entities queried3: ${result3.length}")
  } yield ()
}
