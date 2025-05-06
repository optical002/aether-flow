package aetherflow.engine.performance

import scalafx.*
import scalafx.beans.property.ObjectProperty
import zio.ZLayer

import java.time.Instant

class PerformanceDataAggregator {
  import PerformanceDataAggregator.*
  val windowState = ObjectProperty[Iterable[Entry]](Iterable.empty[Entry])

  def aggregate(newEntries: Iterable[Entry]): Unit = {
    windowState.value = {
      val (constants, others) = (windowState.value ++ newEntries).partition {
        case _: ConstantEntry => true
        case _ => false
      }
      val distinctConstants = constants
        .asInstanceOf[Iterable[ConstantEntry]]
        .groupBy(_.header.asMetricName)
        .flatMap { case (_, group) => group.lastOption }
      distinctConstants ++ others
    }
  }
}
object PerformanceDataAggregator {
  sealed trait Entry
  case class BufferEntry(timestamp: Double, entry: ConstantEntry) extends Entry
  sealed trait ConstantEntry extends Entry { self =>
    def value: Double
    def header: Header

    def valueAsUnitStr: String = self match {
      case UnitConstantEntry(value, _) => f"$value%.2f"
      case NsConstantEntry(value, _)   => {
        val ns = value.toLong
        val seconds = ns / 1_000_000_000
        val msRemainder = ns % 1_000_000_000
        val milliseconds = msRemainder / 1_000_000
        val µsRemainder = msRemainder % 1_000_000
        val microseconds = µsRemainder / 1_000
        val nanoseconds = µsRemainder % 1_000

        s"${seconds}s ${milliseconds}ms ${microseconds}µs ${nanoseconds}ns"
      }
    }
  }
  case class UnitConstantEntry(value: Double, header: Header) extends ConstantEntry
  case class NsConstantEntry(value: Double, header: Header) extends ConstantEntry


  enum Kind(val name: String) {
    case ConstantUnit extends Kind("constantUnit")
    case ConstantNs extends Kind("constantNs")
    case BufferUnit extends Kind("bufferUnit")
    case BufferNs extends Kind("bufferNs")
  }
  case class Header(title: String, name: String) {
    def asMetricName: String = s"$title.$name"
  }

  final case class Path(kind: Kind, header: Header)

  def fromMetricName(metricName: String): Option[Path] = {
    val parts = metricName.split("\\.")
    if (parts.length == 3) {
      val firstPart = parts(0)
      Kind.values.find(_.name == firstPart).map(
        kind => Path(kind, Header(parts(1), parts(2)))
      )
    } else {
      None
    }
  }

  def layer = ZLayer.succeed(new PerformanceDataAggregator)
}
