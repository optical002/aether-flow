package engine.performance

import javafx.collections.ObservableList
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.scene.control.{Label, Tooltip}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color.Grey

import scala.collection.*
import zio.*
import zio.metrics.*
import zio.metrics.connectors.*
import zio.metrics.connectors.internal.*

import java.time.Instant

class PerformanceMonitorWindow(aggregator: PerformanceDataAggregator) extends JFXApp3 {
//  val cssString =
//    """
//      |.onHover{
//      |    -fx-background-color: ORANGE;
//      |}
//      """.stripMargin

  def constantToLabel(constant: PerformanceDataAggregator.ConstantEntry): Label = {
    new Label(s"${constant.header.title}: ${String.format("%.3f", constant.value)}")
  }

  case class ChartLineData(name: String, points: Iterable[(Double, Double)])
  case class ChartData(title: String, lines: Iterable[ChartLineData])

  def dataToLineCharts(data: ChartData): LineChart[Number, Number] = {
    val xAxis = new NumberAxis()
    val yAxis = new NumberAxis()
    val lineChart = new LineChart[Number, Number](xAxis, yAxis)

    lineChart.title = data.title
    xAxis.label = "Time(Seconds)"
    yAxis.label = "Elapsed(Ms)"

    for (e <- data.lines.map(dataToSeries)) {
      lineChart.data().add(e)
    }

    lineChart
  }
  def dataToSeries(chartLineData: ChartLineData): scalafx.scene.chart.XYChart.Series[Number, Number] = {
    val dataPoints = chartLineData.points.map{ (value, time) =>
      XYChart.Data[Number, Number](time, value)
    }
    val series = new XYChart.Series[Number, Number] {
      name = chartLineData.name
      data = dataPoints.toSeq
    }
    series.data.value.forEach { dataPoint =>
      dataPoint.nodeProperty().onChange { (_, _, newNode) =>
        val node: scalafx.scene.Node = newNode
        val tooltip = new Tooltip(
          s"""
             |${chartLineData.name}
             |Ms took: ${dataPoint.YValue.get()} ms
             |At second: ${dataPoint.XValue.get()} s
             |""".stripMargin
        )
        tooltip.setShowDelay(javafx.util.Duration.seconds(0))
        Tooltip.install(node, tooltip)
      }
    }
    series
  }

  def bindToWindowState(scene: Scene) = {
    aggregator.windowState.onChange { (_, _, entries) =>
      val constants = entries.collect {
        case entry: PerformanceDataAggregator.ConstantEntry => entry
      }.groupBy(_.header.title).values.flatMap(_.lastOption)
      val buffers = entries.collect {
        case entry: PerformanceDataAggregator.BufferEntry => entry
      }


      val labels = constants.map(constantToLabel)
      val latestSecond = buffers.maxBy(_.timestamp).timestamp
      val chartData = buffers
        .filter(now => latestSecond - now.timestamp < 10_000)
        .groupBy(_.entry.header.title)
        .map { (title, entries) =>
          val lineData = entries
            .groupBy(_.entry.header.name)
            .map { (name, entries) =>
              ChartLineData(name, entries.map(e => (e.entry.value, e.timestamp)))
            }
          ChartData(title, lineData)
        }
      val lineCharts = chartData.map(dataToLineCharts)
      scene.root = new VBox {
        spacing = 10
        children = labels ++ lineCharts
      }
    }
  }


  def start() = {
    stage = new JFXApp3.PrimaryStage {
      title = "Performance Monitor"
      width = 800
      height = 600
      scene = {
        val scene = new Scene {
          fill = Grey
          content = new Label("Test")
        }
        bindToWindowState(scene)
        scene
      }
    }
  }
}
object PerformanceMonitorWindow {
  def forkNewWindowApp = for {
    aggregator <- ZIO.service[PerformanceDataAggregator]
    window <- ZIO.succeed(new PerformanceMonitorWindow(aggregator))
    fiber <- ZIO.attempt(window.main(Array.empty)).fork
  } yield fiber
}


object Test extends ZIOAppDefault {
  def nextDouble: UIO[Double] = ZIO.random.flatMap(random => random.nextDouble().map(_ * 100))

  val program = for {
    _ <- PerformanceMetricClient.run
    windowFiber <- PerformanceMonitorWindow.forkNewWindowApp
    _ <- {
      def loop: UIO[Unit] = for {
        double <- nextDouble @@ Performance.label(PerformanceDataAggregator.Header("fps", "test"))
        f1 <- nextDouble
        f2 <- nextDouble
        f3 <- nextDouble
        _ <- Performance.timeframe(PerformanceDataAggregator.Header("fps", "fun1"), ZIO.sleep(f1.toInt.millis))
        _ <- Performance.timeframe(PerformanceDataAggregator.Header("fps", "fun2"), ZIO.sleep(f2.toInt.millis))
        _ <- Performance.timeframe(PerformanceDataAggregator.Header("fps", "fun3"), ZIO.sleep(f3.toInt.millis))
        _ <- ZIO.sleep(1.millis)
        _ <- loop
      } yield ()

      loop
    }.fork
    _ <- windowFiber.join
  } yield ()

  def run = program.provide(
    ZLayer.succeed(MetricsConfig(interval = _root_.java.time.Duration.ofMillis(500))),
    PerformanceDataAggregator.layer
  )
}
