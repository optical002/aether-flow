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
  }

  override def graphicsAPI(): GraphicsAPI = opengl.API

  override def render(db: GraphicDatabase): Task[Unit] = for {
    _ <- db.load(GraphicAsset(VertexData.Quad, StandardShader))
  } yield ()

  override def startupWorld(
    builder: WorldBuilder
  ): WorldBuilder = builder
    .addStartUpSystem((world: World) => ZIO.succeed {
      world.createEntity(Transform(0, 0), Velocity(1, 1))
    })
    .addSystem((world: World) => ZIO.succeed {
      val result = world.query2[Transform, Velocity]
      for ((id, t, v) <- result) {
        val moved = t.copy(x = t.x + v.dx, y = t.y + v.dy)
        println(s"Entity $id moved to (${moved.x}, ${moved.y})")
      }
    })
}
