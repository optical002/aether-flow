package aetherflow.engine.performance

object Data {
  object DataAggregator {
    sealed trait Entry
    object Entry {
      case class Buffer(timestamp: Double, entry: Constant) extends Entry
      sealed trait Constant extends Entry {
        self =>
        def value: Double

        def header: Header

        def valueAsUnitStr: String = self match {
          case UnitConstant(value, _) => f"$value%.2f"
          case NsConstant(value, _)   => {
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

      case class UnitConstant(value: Double, header: Header) extends Constant
      case class NsConstant(value: Double, header: Header) extends Constant
    }

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
  }
}
