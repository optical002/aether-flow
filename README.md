# Work in progress
# AetherFlow Game Engine

AetherFlow is a high-performance game engine built on **functional programming** with **ZIO** for concurrency and **ECS** (Entity Component System) for modularity. Inspired by the concept of "Aether," symbolizing purity and space, AetherFlow emphasizes smooth, dynamic interaction and effective management of game systems. It combines the power of **pure effects** and **modular components**, delivering a scalable and fluid architecture for building responsive, concurrent games.

## Getting Started

### Prerequisites

- Scala 3.6.4
- SBT

### Set Up

1. Add the following to your `build.sbt` inside root project settings:

```scala
resolvers += "jitpack" at "https://jitpack.io",
libraryDependencies += "com.github.optical002" % "aether-flow" % "0.1.0-dev"
```

2. Create a simple game setup in `Main.scala`:

```scala
import engine.*
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.WindowConfig
import engine.graphics.opengl.API
import engine.graphics.opengl.shaders.StandardShader
import engine.core.logger.ZIOLogger

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
  } yield ()
}
```

3. Run it and for now it will show performance monitor and display a game window with a rectangle in it :)
