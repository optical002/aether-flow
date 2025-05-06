package aetherflow.engine

import aetherflow.engine.core.{FrameCoordinator, FrameLimiter}
import aetherflow.engine.core.logger.{ASyncLogger, LogFilter}
import aetherflow.engine.ecs.{WorldBuilder, WorldManager}
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.{GraphicDatabase, GraphicsAPI, Window}
import aetherflow.engine.performance.{FrameDurationPerformance, PerformanceMetricClient, PerformanceMonitorWindow}
import aetherflow.engine.resources.Time
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder
  def enableMetrics: Boolean

  private val logger = new ASyncLogger("App")
  
  val program = for {
    _ <- logger.logVerbose("Starting application")
    time <- ZIO.service[Time]
    _ <- logger.logVerbose("Starting counting time")
    _ <- time.startCounting
    _ <- if (enableMetrics) PerformanceMetricClient.run else ZIO.unit

    graphicDB <- ZIO.service[GraphicDatabase]
    _ <- render(graphicDB)
    
    worldManager <- ZIO.service[WorldManager]
    frameLimiter <- ZIO.service[FrameLimiter]

    frameDurationFiber <- FrameDurationPerformance.run
    _ <- logger.logVerbose("Starting 'Performance Monitor'")
    performanceMonitorFiber <- if (enableMetrics) PerformanceMonitorWindow.forkNewWindowApp else ZIO.unit.fork
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
      frameDurationFiber,
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
  
  val configuredLoggedProgram = loggedProgram.provide(
    graphicsAPILayer,
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManager.layer,
    FrameCoordinator.layer,
    Time.layer,
    FrameLimiter.layer(frameRate = configs.frameRate),
    ZLayer.succeed(new LogFilter(
      allowedLogLevel = LogLevel.Debug,
      customScopeRules = Map(
        //        "StartUpSystem" -> LogLevel.Debug,
        "Performance.MonitorWindow" -> LogLevel.All,
        "Performance.MonitorWindow.Metrics" -> LogLevel.None,
      )
    )), // TODO to conf
    ASyncLogger.layer,
    PerformanceMonitorWindow.layer(metricSendIntervalMillis = 40), // TODO to conf
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))

  def run = configuredLoggedProgram
}
