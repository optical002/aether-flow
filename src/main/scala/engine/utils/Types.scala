package engine.utils

import engine.ecs.Component
import zio.*
import zio.stm.*

object Types {
  type TVector[A] = TRef[Vector[A]]
  type RVector[A] = Ref[Vector[A]]
  type CompRef[A <: Component] = NarrowedTRef[Component, A]
}
