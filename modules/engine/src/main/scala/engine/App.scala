package engine

import engine.core.*
import engine.core.logger.{LogFilter, Logger, ZIOLogger}
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.*
import engine.performance.{PerformanceMetricClient, PerformanceMonitorWindow}
import engine.resources.*
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder

  private val logger = new ZIOLogger("App")
  
  val program = for {
    _ <- logger.logVerbose("Starting application")
    time <- ZIO.service[Time]
    _ <- logger.logVerbose("Starting counting time")
    _ <- time.startCounting
    _ <- PerformanceMetricClient.run
    graphicDB <- ZIO.service[GraphicDatabase]
    _ <- render(graphicDB)
    
    worldManager <- ZIO.service[WorldManager]
    frameLimiter <- ZIO.service[FrameLimiter]
    
    _ <- logger.logVerbose("Starting 'Performance Monitor'")
    performanceMonitorFiber <- PerformanceMonitorWindow.forkNewWindowApp // TODO enable/disable via conf
    _ <- logger.logVerbose("Starting 'FrameRate'")
    frameLimiterFiber <- frameLimiter.run
    _ <- logger.logVerbose("Starting 'Time'")
    timeFiber <- time.run
    _ <- logger.logVerbose("Starting 'Window'")
    windowFiber <- Window.run
    _ <- logger.logVerbose("Starting 'ECS-World'")
    ecsWorldFiber <- worldManager.loadWorld(startupWorld(new WorldBuilder))
    _ <- windowFiber.join
    
    _ <- logger.logVerbose("Killing application on window close")
    _ <- close(List(
      ecsWorldFiber,
      frameLimiterFiber,
      timeFiber,
      performanceMonitorFiber
    ))
    _ <- logger.logVerbose("Application killed successfully")
  } yield ()

  def close(fibers: List[Fiber[?, ?]]): UIO[Unit] =
    ZIO.foreachPar(fibers)(_.interruptFork).unit

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
    ZLayer.succeed(new LogFilter(
      allowedLogLevel = LogLevel.All,
      customScopeRules = Map(
        "MovementSystem" -> LogLevel.Info,
      )
    )), // TODO to conf
    ZIOLogger.layer,
    PerformanceMonitorWindow.layer(metricSendIntervalMillis = 500), // TODO to conf
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))
}
