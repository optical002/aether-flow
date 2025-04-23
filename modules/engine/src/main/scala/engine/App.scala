package engine

import engine.components.*
import engine.core.FrameCoordinator
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.*
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder
  
  val program = for {
    graphicDB <- ZIO.service[GraphicDatabase]
    barrier <- FrameCoordinator.makeBarrier(2)
    windowFib <- Window.runProgram(barrier)
    _ <- render(graphicDB)
    worldManager <- ZIO.service[WorldManager]
    ecsWorldFib <- worldManager.loadWorld(barrier, startupWorld(new WorldBuilder))
    _ <- windowFib.join
    _ <- ecsWorldFib.interruptFork
  } yield ()

  def run = program.provide(
    graphicsAPILayer,
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManagerLive.layer
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))
}
