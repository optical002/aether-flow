package engine

import engine.components.*
import engine.core.*
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.*
import engine.resources.*
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder

  private val logger = new Logger("App")
  
  val program = for {
    _ <- logger.logVerbose("Starting application")
    time <- ZIO.service[Time]
    _ <- logger.logVerbose("Starting counting time")
    _ <- time.startCounting
    graphicDB <- ZIO.service[GraphicDatabase]
    _ <- render(graphicDB)
    worldManager <- ZIO.service[WorldManager]
    frameRate <- ZIO.service[FrameLimiter]
    _ <- logger.logVerbose("Starting 'FrameRate'")
    frameRateFiber <- frameRate.run
    _ <- logger.logVerbose("Starting 'Time'")
    timeFiber <- time.run
    _ <- logger.logVerbose("Starting 'Window'")
    windowFiber <- Window.run
    _ <- logger.logVerbose("Starting 'ECS-World'")
    ecsWorldFiber <- worldManager.loadWorld(startupWorld(new WorldBuilder))
    _ <- windowFiber.join
    _ <- logger.logVerbose("Killing application on window close")
    _ <- ecsWorldFiber.interruptFork
    _ <- frameRateFiber.interruptFork
    _ <- timeFiber.interruptFork
    _ <- logger.logVerbose("Application killed successfully")
  } yield ()

  val loggedProgram = program.catchAll { err =>
    logger.logFatal(s"Unhandled exception: ${err.getMessage}")
  }

  def run = loggedProgram.provide(
    graphicsAPILayer,
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManager.layer,
    FrameCoordinator.layer,
    Time.layer,
    FrameLimiter.layer(frameRate = configs.frameRate),
    Logger.layer
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))
}
