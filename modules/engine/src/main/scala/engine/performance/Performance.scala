package engine.performance

import zio.*
import zio.metrics.Metric

object Performance {
  import PerformanceDataAggregator.*
  
  private def metric(kind: Kind, header: Header) = Metric.gauge(
    s"${kind.name}.${header.asMetricName}"
  )

  // Labels
  def labelUnit(header: Header) = metric(Kind.ConstantUnit, header)
  def labelNs(header: Header) = metric(Kind.ConstantNs, header)
  def measureLabel[R, E, A](
    header: Header, f: => ZIO[R, E, A]
  ): ZIO[R, E, A] = for {
    clock <- ZIO.clock
    start <- clock.nanoTime
    result <- f
    end <- clock.nanoTime
    _ <- ZIO.succeed[Double](end - start) @@ metric(Kind.ConstantNs, header)
  } yield result

  // Buffer
  def timeframe[R, E, A](
    header: Header, f: => ZIO[R, E, A]
  ): ZIO[R, E, A] = for {
    clock <- ZIO.clock
    start <- clock.nanoTime
    result <- f
    end <- clock.nanoTime
    _ <- ZIO.succeed[Double](end - start) @@ metric(Kind.BufferNs, header)
  } yield result
}
