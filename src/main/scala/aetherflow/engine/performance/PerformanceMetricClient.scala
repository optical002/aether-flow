package aetherflow.engine.performance

import aetherflow.engine.core.logger.ASyncLogger
import zio.ZIO
import zio.metrics.connectors.MetricEvent
import zio.metrics.connectors.internal.MetricsClient
import zio.metrics.{MetricKey, MetricState}

import java.time.Instant

object PerformanceMetricClient {
  import PerformanceDataAggregator.*
  val logger = new ASyncLogger("Performance.MetricClient")

  val run = for {
    aggregator <- ZIO.service[PerformanceDataAggregator]
    startedAt <- ZIO.succeed(Instant.now().toEpochMilli)
    _ <- logger.logVerbose(s"Starting client")
    _ <- MetricsClient.make { iter =>
      for {
        collected <- ZIO.collectPar(iter) {
          case MetricEvent.New(metricKey, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Unchanged(metricKey, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Updated(metricKey, _, current, timestamp) =>
            ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
        }
        _ <- logger.logVerbose(
          s"Collected ${collected.flatten.size} metrics. Sending to aggregator: " +
          s"${collected.flatten.map {
              case BufferEntry(timestamp, entry) =>
                s"BufferEntry(timestamp = $timestamp, value = ${entry.value}, metricName = ${entry.header.asMetricName})"
              case entry: ConstantEntry  =>
                s"ConstantEntry(value = ${entry.value}, metricName = ${entry.header.asMetricName})"
            }.mkString(start = "[\n  ", sep = "\n  ", end = "\n]")
          }"
        )
        _ <- ZIO.succeed(aggregator.aggregate(collected.flatten))
      } yield ()
    }
  } yield ()


  def processInstance(
    metricKey: MetricKey.Untyped, metricState: MetricState.Untyped, timestamp: Double
  ): Option[Entry] = for {
    path <- fromMetricName(metricKey.name)
    state <- processState(metricState)
  } yield {
    val constantUnit = UnitConstantEntry(state.doubleValue, path.header)
    val constantNs = NsConstantEntry(state.doubleValue, path.header)
    path.kind match {
      case Kind.ConstantUnit => constantUnit
      case Kind.ConstantNs => constantNs
      case Kind.BufferUnit   => BufferEntry(timestamp, constantUnit)
      case Kind.BufferNs   => BufferEntry(timestamp, constantNs)
    }
  }

  def processState(metricState: MetricState.Untyped): Option[Double] = metricState match {
    case MetricState.Counter(_) => None
    case MetricState.Frequency(_) => None
    case MetricState.Gauge(value) => Some(value)
    case MetricState.Histogram(_, _, _, _, _) => None
    case MetricState.Summary(_, _, _, _, _, _) => None
  }
}
