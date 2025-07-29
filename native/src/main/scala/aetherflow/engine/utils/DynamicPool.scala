package aetherflow.engine.utils

import scala.collection.*

class DynamicPool[A] private (
  private val buffer: mutable.Buffer[A],
  private val chunkSize: Int,
  private var currentSize: Int,
  create: => A,
) {
  private var toTakeIdx: Int = 0

  private def take: A = {
    if (buffer.length == toTakeIdx) {
      buffer ++= (1 to chunkSize).map(_ => create)
      currentSize += chunkSize
    }
    val result = buffer.remove(toTakeIdx)
    toTakeIdx += 1
    result
  }
  private def release(): Unit = {
    if (toTakeIdx > 0) {
      toTakeIdx -= 1
    } else {
      // add logger later on
      println("Warning: DynamicPool is released when nothing should be released")
    }
  }

  @inline def acquire(use: A => Unit): Unit = {
    val resource = take
    try {
      use(resource)
    } finally {
      release()
    }
  }

  def takeEntry = DynamicPool.Entry[A](take, release)
}
object DynamicPool {
  case class Entry[A](resource: A, release: () => Unit)

  def create[A](initialSize: Int, create: => A): DynamicPool[A] = {
    new DynamicPool[A](
      buffer = mutable.Buffer.fill(initialSize)(create),
      chunkSize = initialSize,
      currentSize = initialSize,
      create = create
    )
  }
}
