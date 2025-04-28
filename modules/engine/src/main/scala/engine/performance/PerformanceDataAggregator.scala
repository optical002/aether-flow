package engine.performance

import zio.ZLayer

import java.time.Instant
import scalafx.*
import scalafx.beans.property.ObjectProperty

class PerformanceDataAggregator {
  import PerformanceDataAggregator.*
  val windowState = ObjectProperty[Iterable[Entry]](Iterable.empty[Entry])

  def aggregate(newEntries: Iterable[Entry]): Unit = {
    println(s"aggregating $newEntries")
    windowState.value = windowState.value ++ newEntries
  }
}
object PerformanceDataAggregator {
  sealed trait Entry
  case class BufferEntry(timestamp: Double, entry: ConstantEntry) extends Entry
  case class ConstantEntry(value: Double, header: Header) extends Entry

  enum Kind(val name: String) {
    case Constant extends Kind("constant")
    case Buffer extends Kind("buffer")
  }
  case class Header(title: String, name: String) {
    def asMetricName: String = s"$title.$name"
  }

  final case class Path(kind: Kind, header: Header)

  def fromMetricName(metricName: String): Option[Path] = {
    val parts = metricName.split("\\.")
    println(s"$metricName -> ${parts.length}")
    if (
      parts.length == 3
    ) {
      val firstPart = parts(0)
      Kind.values.find(_.name == firstPart).map(
        kind => Path(kind, Header(parts(1), parts(2)))
      )
    } else {
      println("none")
      None
    }
  }

  def layer = ZLayer.succeed(new PerformanceDataAggregator)
}
