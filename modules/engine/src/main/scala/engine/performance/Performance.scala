package engine.performance

import zio.*
import zio.metrics.Metric

object Performance {
  import PerformanceDataAggregator.*
  
  private def metric(kind: Kind, header: Header) = Metric.gauge(
    s"${kind.name}.${header.asMetricName}"
  )

  def label(header: Header) = metric(Kind.Constant, header)

  def timeframe[R, E, A](
    header: Header, f: => ZIO[R, E, A]
  ): ZIO[R, E, A] = for {
    clock <- ZIO.clock
    start <- clock.nanoTime
    result <- f
    end <- clock.nanoTime
    _ <- ZIO.succeed[Double]((start - end) / 1_000_000) @@ metric(Kind.Buffer, header)
  } yield result
}
