package aetherflow.engine.utils

import scala.collection.mutable

// Note this is not an atomic memo, using it in between fibers may crash.
trait Memo[Key, Value] {
  def get(key: Key): Value
}
object Memo {
  def create[Key, Value](produce: Key => Value): Memo[Key, Value] = {
    val collection = mutable.Map.empty[Key, Value]

    (key: Key) => {
      collection.get(key) match {
        case Some(value) => value
        case None        =>
          val newValue = produce(key)
          collection.put(key, newValue)
          newValue
      }
    }
  }
}
