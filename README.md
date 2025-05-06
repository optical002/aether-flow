
[![](https://jitpack.io/v/optical002/aether-flow.svg)](https://jitpack.io/#optical002/aether-flow)
# AetherFlow Game Engine

An attempt to make a purely functional game engine, using ZIO effects and an ECS architecture, which would be extremely 
simple and convenient to build scalable games fast. Convenience and game development speed are the main goals of
this project. 

Currently work in progress.

## Getting Started

### Set Up

Add the following to your `build.sbt` inside project settings:


```scala
resolvers += "jitpack" at "https://jitpack.io",
libraryDependencies += "com.github.optical002" % "aether-flow" % "0.0.1"
```

### Running simple game window

```scala
package game

import engine.*
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.WindowConfig
import engine.graphics.opengl.API
import engine.graphics.opengl.shaders.StandardShader
import engine.core.logger.ASyncLogger

import zio.*

object GameMain extends App {
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

object StartUpSystem extends System {
  override def run(world: World, logger: ASyncLogger): Task[Unit] =
    world.createEntity(Transform(0, 0), Velocity(1, 1))

  *> logger
.logDebug("Created entity")
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
```

### Building windows executable

Note: If somehting does not work or break, you can look up example projects which are built specific versions inside my ['aether-flow'](https://github.com/optical002/aether-flow-examples) repository.

1. Create `plugins.sbt` inside `project` directory and add following

```scala
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.3")
```

2. In `build.sbt` inside project settings add main entry point of the game (replace `"Main"` with your main class name)

```scala
assembly / mainClass := Some("Main"),
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
},
```

4. To install build tools run this from powershell

```shell
iwr -useb https://github.com/optical002/aether-flow-tooling/releases/download/build-tools-v0.1.0/install.ps1 | iex
```

5. To build execute `build.ps1`
