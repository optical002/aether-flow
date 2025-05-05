package engine.ecs

import scala.collection.{Map, Set, mutable}
import engine.ecs.Data.*
import engine.utils.Types.*

import zio.*
import zio.stm.*

class Archetype private(
  val componentTypes: Set[Class[? <: Component]],
  val entities: TVector[Entity],
  val components: TMap[Class[? <: Component], TVector[TRef[Component]]]
) {
  def addEntity(entity: Entity, comps: Map[Class[? <: Component], Component]): UIO[Unit] = for {
    _ <- entities.update(_.appended(entity)).commit
    _ <- STM.foreach(comps.toVector) { case (targetCls, compToAdd) =>
      components.find {
        case (cls, comps) if cls == targetCls => comps
      }.flatMap {
        case Some(comps) => 
          for {
            compRef <- TRef.make(compToAdd)
            _ <- comps.update(_.appended(compRef))
          } yield ()
        case None =>
          for {
            compRef <- TRef.make(compToAdd)
            comps <- TRef.make(Vector(compRef))
            _ <- components.put(targetCls, comps)
          } yield ()
      }
    }.commit
  } yield ()

  def subsetOf(query: Set[Class[? <: Component]]): Boolean = 
    query.subsetOf(componentTypes)
}
object Archetype {
  def createWithEntity(
    componentTypes: Set[Class[? <: Component]],
    entity: Entity, compsMap: Map[Class[? <: Component], Component]
  ): USTM[Archetype] = for {
    entities <- TRef.make(Vector(entity))
    compsMapRef <- STM.foreach(compsMap.toVector) { case (cls, comp) =>
      for {
        refComp <- TRef.make(comp)
        vectorComps <- TRef.make(Vector(refComp))
      } yield (cls , vectorComps)
    }
    components <- TMap.make(compsMapRef.toMap.toSeq*)
  } yield Archetype(componentTypes, entities, components)
}
