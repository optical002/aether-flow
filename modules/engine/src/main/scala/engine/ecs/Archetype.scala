package engine.ecs

import scala.collection.{Map, Set, mutable}
import engine.ecs.Data.*

class Archetype(val componentTypes: Set[Class[? <: Component]]) {
  val entities: mutable.Buffer[Entity] = mutable.Buffer()
  val components: mutable.Map[Class[? <: Component], mutable.Buffer[Component]] =
    mutable.Map.empty

  def addEntity(entity: Entity, comps: Map[Class[? <: Component], Component]): Unit = {
    entities += entity

    comps.foreach { case (cls, comp) =>
      if (!components.contains(cls)) {
        components(cls) = mutable.Buffer[Component]()
      }
      components(cls) += comp
    }
  }

  def matches(query: Set[Class[? <: Component]]): Boolean =
    query.subsetOf(componentTypes)
}
