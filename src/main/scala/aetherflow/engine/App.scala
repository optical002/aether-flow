package aetherflow.engine

import aetherflow.engine.*
import aetherflow.engine.core.{FrameCoordinator, FrameLimiter}
import aetherflow.engine.core.logger.{ASyncLogger, LogFilter}
import aetherflow.engine.ecs.{WorldBuilder, WorldManager}
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.{GraphicDatabase, GraphicsAPI, Window}
import aetherflow.engine.resources.Time
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder
  def createLogFilter: LogFilter = new LogFilter(LogLevel.Debug)

  private val logger = new ASyncLogger("App")
  
  val program = for {
    _ <- logger.logVerbose("Starting application")
    time <- ZIO.service[Time]
    _ <- time.startCounting
    graphicDB <- ZIO.service[GraphicDatabase]
    _ <- render(graphicDB)
    frameDurationFiber <- performance.FrameDuration.run
    frameLimiter <- ZIO.service[FrameLimiter]
    frameLimiterFiber <- frameLimiter.run
    timeFiber <- time.run
    worldManager <- ZIO.service[WorldManager]
    ecsWorldFiber <- worldManager.loadWorld(startupWorld(new WorldBuilder))
    windowFiber <- Window.run.fork
    _ <- windowFiber.join
    _ <- logger.logVerbose("Killing application on window close")
    _ <- ZIO.foreachPar(Vector(
      frameDurationFiber,
      ecsWorldFiber,
      frameLimiterFiber,
      timeFiber,
    ))(_.interruptFork)
    _ <- logger.logVerbose("Application killed successfully")
  } yield ()

  val loggedProgram = program.catchAll { err =>
    logger.logFatal(s"Unhandled exception: ${err.getMessage}")
  }

  val configuredLoggedProgram = loggedProgram.provide(
    ZLayer(ZIO.attempt(graphicsAPI())),
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManager.layer,
    FrameCoordinator.layer,
    Time.layer,
    FrameLimiter.layer(frameRate = configs.frameRate),
    ZLayer.succeed(createLogFilter),
    ASyncLogger.layer,
  )

  def run = configuredLoggedProgram
}
