package aetherflow.engine.graphics

import zio.*
import zio.stm.*

import scala.collection.*
import scala.util.*

trait GraphicDatabaseAPI {
  def load(graphic: GraphicAsset): UIO[Promise[Throwable, GraphicHandle]]
  def unload(handle: GraphicHandle): Task[Unit]
}
class GraphicDatabase private(
  toLoad: TQueue[(GraphicAsset, Promise[Throwable, GraphicHandle])],
  loaded: TMap[GraphicHandle, GraphicAsset],
  api: GraphicsAPI,
) extends GraphicDatabaseAPI {
  override def load(graphic: GraphicAsset): UIO[Promise[Throwable, GraphicHandle]] = for {
    promise <- Promise.make[Throwable, GraphicHandle]
    _ <- toLoad.offer((graphic, promise)).commit
  } yield promise

  override def unload(handle: GraphicHandle): Task[Unit] = for {
    _ <- ZIO.attempt(handle.unload())
    _ <- loaded.delete(handle).commit
  } yield ()

  def initializeQueuedUpAssets(): UIO[Unit] = for {
    pairs <- toLoad.takeAll.commit.flatMap(_.mapZIO { case (asset, promise) =>
      val (maybeHandle, pExecution) = Try(api.loadAsset(asset)) match {
        case Success(value)     => (Some(value), promise.succeed(value))
        case Failure(exception) => (None, promise.fail(exception))
      }
      pExecution.map(_ => maybeHandle -> asset)
    })
    _ <- STM.collectAll(pairs.collect {
      case (Some(handle), asset) => loaded.put(handle, asset)
    }.toList).commit
  } yield ()

  def renderAllAssets(): Task[Unit] = for {
    _ <- loaded.foreach((handle, _) => STM.attempt(handle.render())).commit
  } yield ()

  def unloadAll(): Task[Unit] = for {
    graphicList <- loaded.toList.commit
    _ <- ZIO.collectAll(graphicList.map((handle, _) => ZIO.attempt(handle.unload())))
  } yield ()
}
object GraphicDatabase {
  def makeZIO(api: GraphicsAPI) = for {
    toLoad <- TQueue.unbounded[(GraphicAsset, Promise[Throwable, GraphicHandle])].commit
    loaded <- TMap.empty[GraphicHandle, GraphicAsset].commit
  } yield GraphicDatabase(toLoad, loaded, api)
  
  def layer = ZLayer {for {
    api <- ZIO.service[GraphicsAPI]
    db <- makeZIO(api)
  } yield db}
}