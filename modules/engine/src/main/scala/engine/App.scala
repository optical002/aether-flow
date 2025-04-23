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
  
  val program = for {
    time <- ZIO.service[Time]
    _ <- time.startCounting
    graphicDB <- ZIO.service[GraphicDatabase]
    _ <- render(graphicDB)
    worldManager <- ZIO.service[WorldManager]
    frameRate <- ZIO.service[FrameRate]
    frameRateFiber <- frameRate.run
    timeFiber <- time.run
    windowFiber <- Window.run
    ecsWorldFiber <- worldManager.loadWorld(startupWorld(new WorldBuilder))
    _ <- windowFiber.join
    _ <- ecsWorldFiber.interruptFork
    _ <- frameRateFiber.interruptFork
    _ <- timeFiber.interruptFork
  } yield ()

  def run = program.provide(
    graphicsAPILayer,
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManager.layer,
    FrameCoordinator.layer,
    Time.layer,
    FrameRate.layer(frameRate = configs.frameRate),
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))
}
