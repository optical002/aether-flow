package engine.performance

import zio.ZIO
import zio.metrics.{MetricKey, MetricState}
import zio.metrics.connectors.MetricEvent
import zio.metrics.connectors.internal.MetricsClient

import java.time.Instant

object PerformanceMetricClient {
  import PerformanceDataAggregator.*

  val run = for {
    aggregator <- ZIO.service[PerformanceDataAggregator]
    startedAt <- ZIO.succeed(Instant.now().toEpochMilli)
    _ <- MetricsClient.make { iter =>
      for {
        collected <- ZIO.collectPar(iter) {
          case MetricEvent.New(metricKey, current, timestamp) =>
            ZIO.succeed(s"${timestamp.toEpochMilli}").debug
              *> ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Unchanged(metricKey, current, timestamp) =>
            ZIO.succeed(s"${timestamp.toEpochMilli}").debug
              *> ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
          case MetricEvent.Updated(metricKey, _, current, timestamp) =>
            ZIO.succeed(s"${timestamp.toEpochMilli}").debug
              *> ZIO.succeed(processInstance(metricKey, current, timestamp.toEpochMilli - startedAt))
        }
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
    val constant = ConstantEntry(state.doubleValue, path.header)
    path.kind match {
      case Kind.Constant => constant
      case Kind.Buffer   => BufferEntry(timestamp, constant)
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
