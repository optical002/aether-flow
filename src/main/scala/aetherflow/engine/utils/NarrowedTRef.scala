package aetherflow.engine.utils

import zio.*
import zio.stm.*

class NarrowedTRef[Parent, Child <: Parent] private(ref: TRef[Parent]) {
  def get: USTM[Child] = ref.get.map(_.asInstanceOf[Child])

  def getAndSet(a: Child): USTM[Child] =
    ref.getAndSet(a).map(_.asInstanceOf[Child])

  def getAndUpdate(f: Child => Child): USTM[Child] =
    ref.getAndUpdate(cmp => f(cmp.asInstanceOf[Child])).map(_.asInstanceOf[Child])

  def getAndUpdateSome(f: PartialFunction[Child, Child]): USTM[Child] =
    ref.getAndUpdateSome(cmp => f(cmp.asInstanceOf[Child])).map(_.asInstanceOf[Child])

  def modify[B](f: Child => (B, Child)): USTM[B] =
    ref.modify(cmp => f(cmp.asInstanceOf[Child]))

  def modifySome[B](default: B)(f: PartialFunction[Child, (B, Child)]): USTM[B] =
    ref.modifySome(default)(cmp => f(cmp.asInstanceOf[Child]))

  override def toString: String = ref.toString

  def update(f: Child => Child): USTM[Unit] =
    ref.update(cmp => f(cmp.asInstanceOf[Child]))

  def updateAndGet(f: Child => Child): USTM[Child] =
    ref.updateAndGet(cmp => f(cmp.asInstanceOf[Child])).map(_.asInstanceOf[Child])

  def updateSome(f: PartialFunction[Child, Child]): USTM[Unit] =
    ref.updateSome(cmp => f(cmp.asInstanceOf[Child]))

  def updateSomeAndGet(f: PartialFunction[Child, Child]): USTM[Child] =
    ref.updateSomeAndGet(cmp => f(cmp.asInstanceOf[Child])).map(_.asInstanceOf[Child])
}
object NarrowedTRef {
  def create[Parent, Child <: Parent](
    ref: TRef[Parent]
  ): USTM[Either[Throwable, NarrowedTRef[Parent, Child]]] = ref.get.map { parent =>
    if (parent.isInstanceOf[Child]) {
      Right(NarrowedTRef(ref))
    } else {
      Left(new RuntimeException("Failed to narrow down TRef[Parent] to NarrowedTRef[Child]."))
    }
  }
}
