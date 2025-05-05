package engine.utils

import engine.ecs.Component
import engine.utils.Types.CompRef
import zio.stm.{TRef, USTM}

object Extensions {
  extension[Parent](ref: TRef[Parent]) {
    def narrow[Child <: Parent]: USTM[Either[Throwable, NarrowedTRef[Parent, Child]]] =
      NarrowedTRef.create(ref)
  }
  extension (ref: TRef[Component]) {
    def narrowComp[A <: Component]: USTM[Either[Throwable, CompRef[A]]] = ref.narrow
  }
  extension[A] (ustm: USTM[Either[Throwable, A]]) {
    def getOrThrow: USTM[A] = ustm.map {
      case Right(a) => a
      case Left(throwable) => throw throwable
    }
  }
}
