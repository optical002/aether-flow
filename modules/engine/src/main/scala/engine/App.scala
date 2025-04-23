package engine

import engine.components.*
import engine.ecs.*
import engine.graphics.*
import engine.graphics.config.*
import zio.*

abstract class App extends ZIOAppDefault {
  lazy val configs: WindowConfig
  def graphicsAPI(): GraphicsAPI
  def render(db: GraphicDatabase): Task[Unit]
  def startupWorld(builder: WorldBuilder): WorldBuilder
  
  // TODO make it so closing window stops the whole program.
  val program = for {
    graphicDB <- ZIO.service[GraphicDatabase]
    windowFib <- Window.forkWindow
    _ <- render(graphicDB)
    worldManager <- ZIO.service[WorldManager]
    ecsWorldFib <- worldManager.loadWorld(startupWorld(new WorldBuilder))
    _ <- windowFib.join zip ecsWorldFib.join
  } yield ()

  def run = program.provide(
    graphicsAPILayer,
    ZLayer.succeed(configs),
    GraphicDatabase.layer,
    WorldManagerLive.layer
  )

  val graphicsAPILayer = ZLayer(ZIO.attempt(graphicsAPI()))
}
