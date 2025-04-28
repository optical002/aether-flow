package engine.core

import zio.*
import zio.metrics.*
import zio.metrics.connectors.*
import zio.metrics.connectors.internal.*

object Metrics_ extends ZIOAppDefault {
  val logger = new Logger("Metrics")

  val client = MetricsClient.make { iter =>
    for {
      _ <- logger.logVerbose("start")
      _ <- ZIO.foreach(iter) { a => 
        logger.logVerbose(a.toString)
        a match {
          case MetricEvent.New(metricKey, current, timestamp)               => current match {
            case MetricState.Counter(count)                                  => ???
            case MetricState.Frequency(occurrences)                          => ???
            case MetricState.Gauge(value)                                    => ???
            case MetricState.Histogram(buckets, count, min, max, sum)        => ???
            case MetricState.Summary(error, quantiles, count, min, max, sum) => ???
          }
          case MetricEvent.Unchanged(metricKey, current, timestamp)         => ???
          case MetricEvent.Updated(metricKey, oldState, current, timestamp) => ???
        }
      }
      _ <- logger.logVerbose("end")
    } yield ()
  }

  val layer = ZLayer.service[Int]

  def program = for {
    _ <- client
    _ <- logger.logVerbose("program start")
    asd <- ZIO.succeed(2.0) @@ Metric.gauge("a")
    _ <- ZIO.sleep(1.seconds)
    asd <- ZIO.succeed(3.0) @@ Metric.gauge("a")
    _ <- ZIO.sleep(1.seconds)
    asd <- ZIO.succeed(4.0) @@ Metric.gauge("a")
    _ <- ZIO.sleep(1.seconds)
    asd <- ZIO.succeed(4.0) @@ Metric.gauge("a")
    _ <- ZIO.sleep(1.seconds)
    asd <- ZIO.succeed(5.0) @@ Metric.gauge("a")
    _ <- logger.logVerbose("program end")
    _ <- ZIO.never
  } yield ()

  def run = program.provide(
    ZLayer.succeed(MetricsConfig(interval = _root_.java.time.Duration.ofSeconds(10))),
    Logger.layer,
  )
}
