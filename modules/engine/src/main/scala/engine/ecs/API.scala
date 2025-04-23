package engine.ecs

import scala.collection.*
import zio.*

case class Transform(x: Float, y: Float) extends Component
case class Velocity(dx: Float, dy: Float) extends Component

trait System {
  def run(world: World): Task[Unit]
}

trait WorldManager {
  def loadWorld(worldBuilder: WorldBuilder): Task[Fiber[Throwable, Unit]]
}
class WorldManagerLive extends WorldManager {
  override def loadWorld(
    worldBuilder: WorldBuilder
  ): UIO[Fiber[Throwable, Unit]] = 
    worldBuilder.launchWorld().fork
}
object WorldManagerLive {
  val layer = ZLayer.succeed(new WorldManagerLive)
}

class WorldBuilder {
  private var startUpSystems: mutable.Buffer[System] = mutable.Buffer[System]()
  private var updateSystems: mutable.Buffer[System] = mutable.Buffer[System]()

  def addStartUpSystem(system: System): WorldBuilder = {
    startUpSystems += system
    this
  }

  def addSystem(system: System): WorldBuilder = {
    updateSystems += system
    this
  }

  def launchWorld(): Task[Unit] = {
    val world = new World

    for {
      _ <- ZIO.foreachPar(startUpSystems.toList)(_.run(world))
      // loop
      _ <- {
        def loop: Task[Unit] = for {
          _ <- ZIO.succeed("ECS Frame Start").debug
          _ <- ZIO.foreachPar(updateSystems.toList)(_.run(world))
          _ <- ZIO.succeed("ECS Frame End").debug
          _ <- ZIO.sleep(16.millis)
          _ <- loop
        } yield ()
        
        loop
      }
    } yield ()
  }
}

trait FrameCoordinator {
  // 1. receive window ready
  // 2. receive ecs ready
  // 3. tell ecs/window it's ready for frame process
  // 4. wait until frame rate or both are ready
}

object Test {
  def main(args: Array[String]): Unit = {
    val world = new World

    // Create some entities
    world.createEntity(Transform(0, 0), Velocity(1, 1))
    world.createEntity(Transform(5, 5), Velocity(-1, -1))
    world.createEntity(Transform(100, 100)) // No velocity

    // Query for entities with both Transform and Velocity
    val result = world.query2[Transform, Velocity]
    for ((id, t, v) <- result) {
      val moved = t.copy(x = t.x + v.dx, y = t.y + v.dy)
      println(s"Entity $id moved to (${moved.x}, ${moved.y})")
    }
  }
}