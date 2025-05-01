package engine.ecs

import scala.collection.{Map, Seq, mutable}
import scala.reflect.ClassTag
import engine.ecs.Data.*

class World extends QueryExts {
  private var nextEntityId: Long = 0
  private val archetypes = mutable.Buffer[Archetype]()

  def createEntity(components: Component*): Entity = {
    val entity = nextEntityId
    nextEntityId += 1

    val types  = components.map(_.getClass).toSet
    val compsMap: Map[Class[? <: Component], Component] = components.map(c => c.getClass -> c).toMap

    val arch = archetypes.find(_.componentTypes == types)
      .getOrElse {
        val newArch = new Archetype(types)
        archetypes += newArch
        newArch
      }


    arch.addEntity(entity, compsMap)
    entity
  }

  def queryGeneric(components: ClassTag[? <: Component]*): Seq[(Entity, Seq[Component])] = {
    val componentTypes = components.map(_.runtimeClass.asInstanceOf[Class[? <: Component]])
    archetypes
      .filter(_.matches(componentTypes.toSet))
      .flatMap { arch =>
        val componentData = componentTypes.map { compType =>
          arch.components(compType).asInstanceOf[Seq[Component]]
        }
        arch.entities.zip(componentData.transpose).map { case (e, comps) =>
          (e, comps)
        }
      }
  }
}
