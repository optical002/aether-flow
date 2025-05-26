package aetherflow.engine.ecs

import zio.*
import zio.stm.*
import aetherflow.engine.ecs.Data.*
import aetherflow.engine.utils.Types.*
import scala.collection.*
import scala.reflect.ClassTag

class EcsStateMachine private(
  archetypesRef: TVector[Archetype]
) extends QueryExts {
  private var nextEntityId: Long = 0

  // TODO check if components are not unique, then return an error.
  def createEntity(components: Component*): UIO[Entity] = {
    val entity = nextEntityId
    nextEntityId += 1

    val types = components.map(_.getClass).toSet
    val compsMap: Map[Class[? <: Component], Component] = components.map(c => c.getClass -> c).toMap
    
    val maybeLeftToAddEntity = for {
      archetypes <- archetypesRef.get
      arch <- archetypes.find(_.componentTypes == types) match {
        case Some(arch) => 
          // Add entity later to not block every archetype from other fibers.
          ZSTM.succeed(Some(arch))
        case None => 
          Archetype.createWithEntity(types, entity, compsMap).flatMap { newArch =>
            archetypesRef.update(_.appended(newArch))
          }.map(_ => None)
      }
    } yield arch
    
    maybeLeftToAddEntity.commit.flatMap {
      case Some(arch) => arch.addEntity(entity, compsMap)
      case None => ZIO.unit
    }.map(_ => entity)
  }

  // TODO check if components are not unique, then return an error.
  def queryGeneric(components: ClassTag[? <: Component]*): UIO[Seq[(Entity, Seq[TRef[Component]])]] = {
    val componentTypes = components.map(_.runtimeClass.asInstanceOf[Class[? <: Component]])
    
    for {
      archetypes <- archetypesRef.get.commit
      data <- ZIO.foreach(archetypes.filter(_.subsetOf(componentTypes.toSet))) { arch =>
        val transaction = for {
          componentRefs <- STM.foreach(componentTypes) { cls => // cls for example [Transform, Vector]
            arch.components.get(cls).map(_.getOrElse(
              throw new RuntimeException(
                "Should not happen, we have already filtered every archetype to contain only valid components"
              )
            ))
          }
          // for example componentLists = [
          //   Transform -> [Entity1, Entity2, Entity3]
          //   Vector    -> [Entity1, Entity2, Entity3]
          // ]
          componentLists <- STM.foreach(componentRefs)(_.get)
          entities <- arch.entities.get
        } yield (entities, componentLists)

        // Move computations outside transaction, some performance increase, because less time to block other
        // fibers from accessing stm's.
        transaction.commit.map { case (entities, componentLists) => 
          entities.zip(componentLists.transpose)
        }
      }
    } yield data.flatten
  }
}
object EcsStateMachine {
  val create: UIO[EcsStateMachine] = TRef.make(Vector.empty[Archetype]).map(EcsStateMachine(_)).commit
}