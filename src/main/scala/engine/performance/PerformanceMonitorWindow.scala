package engine.performance

import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{HBox, Pane, StackPane, VBox}
import scalafx.stage.{Stage, StageStyle}
import engine.core.logger.{SyncLogger, ASyncLogger}
import engine.utils.*
import scalafx.Includes.*
import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.property.StringProperty
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Scene
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.scene.control.{Label, Tooltip}
import scalafx.scene.paint.Color
import scalafx.scene.text.*
import scalafx.stage.StageStyle.{Undecorated, Utility}
import zio.*
import zio.metrics.*
import zio.metrics.connectors.*

import scala.collection.*

class PerformanceMonitorWindow(
  aggregator: PerformanceDataAggregator,
  logger: SyncLogger,
  killSwitch: () => Unit,
) extends JFXApp3 {
  val metricLogger = logger.scope("Metrics")

  val chartMemo = Memo.create[
    ChartTitle, LineChart[Number, Number]
  ] { title =>
    val xAxis = new NumberAxis()
    xAxis.autoRanging = false
    xAxis.lowerBound = 0
    xAxis.upperBound = 10
    val yAxis = new NumberAxis()

    val lineChart = new LineChart[Number, Number](xAxis, yAxis)

    title.setTitle(lineChart.title)
    lineChart.animated = false
    lineChart.createSymbols = false
    xAxis.label = "Last 10 seconds"
    yAxis.label = "Elapsed in ns"

    lineChart
  }
  val linesMemo = Memo.create[
    (ChartTitle, ChartLineName), XYChart.Series[Number, Number]
  ] { (_, chartLineName) =>
    // Create 'emptyDataPoint' as Iterable, not as Seq, because of scalafx and javafx ambiguity.
    val emptyDataPoint: Iterable[javafx.scene.chart.XYChart.Data[Number, Number]] = Iterable.empty
    val series = new XYChart.Series[Number, Number] {
      name = chartLineName.a
      data = emptyDataPoint.toSeq
    }
    val subscriptionList = mutable.Buffer[Subscription]()
    series.data.onChange { (_, _, dataList) =>
      subscriptionList.foreach(_.cancel())
      dataList.forEach { dataPoint =>
        val subscription = dataPoint.nodeProperty().onChange { (_, _, newNode) =>
          val node: scalafx.scene.Node = newNode
          val tooltip = new Tooltip(
            s"""
               |${chartLineName.a}
               |Ns took: ${dataPoint.YValue.get()} ns
               |At second: ${dataPoint.XValue.get()} s
               |""".stripMargin
          )
          tooltip.setShowDelay(javafx.util.Duration.seconds(0))
          Tooltip.install(node, tooltip)
        }
        subscriptionList.addOne(subscription)
      }
    }
    series
  }

  case class ChartTitle(setTitle: StringProperty => Unit)
  case class ChartLineName(a: String)

  case class ChartLineData(name: ChartLineName, points: Iterable[(Double, Double)])
  case class ChartData(title: ChartTitle, lines: Iterable[ChartLineData])

  def dataToLineCharts(data: ChartData): LineChart[Number, Number] = {
    val lineChart = chartMemo.get(data.title)
    val lines: Iterable[javafx.scene.chart.XYChart.Series[Number, Number]] = data.lines.map { lineData =>
      val line = linesMemo.get((data.title, lineData.name))
      // Create 'dataPoints' as Iterable, not as Seq, because of scalafx and javafx ambiguity.
      val dataPoints: Iterable[javafx.scene.chart.XYChart.Data[Number, Number]] = lineData.points.map { (value, time) =>
        XYChart.Data[Number, Number](time, value)
      }
      line.data = dataPoints.toSeq
      line
    }
    lineChart.data = lines.toSeq

    lineChart
  }


  def bindToWindowState(contentBox: VBox) = {
    import engine.performance.PerformanceDataAggregator.*

    aggregator.windowState.onChange { (_, _, entries) => Platform.runLater {
      killSwitch()
      metricLogger.logVerbose(
        s"Aggregated ${entries.size} metrics from aggregator: " +
          s"${entries.map {
            case BufferEntry(timestamp, entry) =>
              s"BufferEntry(timestamp = $timestamp, value = ${entry.value}, metricName = ${entry.header.asMetricName})"
            case entry: ConstantEntry =>
              s"ConstantEntry(value = ${entry.value}, metricName = ${entry.header.asMetricName})"
          }.mkString(start = "[\n  ", sep = "\n  ", end = "\n]")
          }"
      )
      val constants = entries.collect {
        case entry: PerformanceDataAggregator.ConstantEntry => entry
      }.groupBy(_.header.title)
      val labels = constants.flatMap { case(title, entries) =>
        val titleLabel = new Label(title) {
          font = Font.font("System", FontWeight.Bold, 20) // 20 pt bold
        }
        val entriesLabel = entries.map { entry =>
          new Label(s"${entry.header.name}: ${entry.valueAsUnitStr}")
        }

        Seq(titleLabel) ++ entriesLabel
      }


      val buffers = entries.collect {
        case entry: PerformanceDataAggregator.BufferEntry => entry
      }
      val latestMillis = buffers.maxBy(_.timestamp).timestamp
      val latestSecond = latestMillis / 1000
      val chartData = buffers
        .filter(now => latestMillis - now.timestamp < 10_000)
        .groupBy(_.entry.header.title)
        .flatMap { (title, entries) =>
          val filtered = entries.filter(_.timestamp >= latestMillis)
          if (filtered.isEmpty) None else {
            val lineData = entries
              .groupBy(_.entry.header.name)
              .map { (name, entries) =>
                ChartLineData(ChartLineName(name), entries.map(e => (
                  e.entry.value, (e.timestamp / 1000.0) - (latestSecond - 10.0)
                )))
              }
            val maxEntry = filtered.maxBy(_.entry.value).entry
            Some(ChartData(
              ChartTitle { prop =>
                prop.value = s"$title: ${maxEntry.valueAsUnitStr}"
              },
              lineData
            ))
          }
        }
      val lineCharts = chartData.map(dataToLineCharts)

      contentBox.children = labels ++ lineCharts
    }}
  }

  lazy val titleBar = {
    var dragDeltaX = 0.0
    var dragDeltaY = 0.0

    val closeButton = new Button("X") {
      onMousePressed = _ => stage.close()
      style =
        """
          | -fx-background-color: linear-gradient(#3a3a3a, #1f1f1f);
          | -fx-text-fill: white;
          | -fx-font-weight: bold;
          | -fx-border-color: #5e5e5e;
          | -fx-border-width: 1;
          | -fx-border-radius: 3;
          | -fx-background-radius: 3;
          | -fx-padding: 5 10 5 10;
          """.stripMargin

      onMouseEntered = _ => style.value = style.value + "\n -fx-background-color: linear-gradient(#505050, #2a2a2a);"
      onMouseExited  = _ => style.value = style.value + "\n -fx-background-color: linear-gradient(#3a3a3a, #1f1f1f);"
    }

    new HBox {
      padding = Insets(0, 5, 5, 5) // top, right, bottom, left — top = 0
      spacing = 10
      alignment = Pos.CenterRight
      style = "-fx-background-color: #444"
      children = Seq(closeButton)
      // Set mouse pressed and dragged events directly
      this.onMousePressed = (event: MouseEvent) => {
        dragDeltaX = event.screenX - stage.x.value
        dragDeltaY = event.screenY - stage.y.value
      }

      this.onMouseDragged = (event: MouseEvent) => {
        stage.x = event.screenX - dragDeltaX
        stage.y = event.screenY - dragDeltaY
      }
    }
  }

  def start() = {
    val contentBox = new VBox {
//      style = "-fx-background-color: #2f2f2f;"  // Dark grey background color
      children = Seq(new Label("Starting..."))
    }
    stage = new JFXApp3.PrimaryStage {
      title = "Performance Monitor"
      width = 1080
      height = 1080
      initStyle(Undecorated)
      scene = new Scene {
        fill = Color.Black
        root = new VBox {
          children = Seq(
            titleBar,
            contentBox
          )
        }
      }
    }
    bindToWindowState(contentBox)
  }
}
object PerformanceMonitorWindow {
  val logger = new ASyncLogger("Performance.MonitorWindow")

  // A hack to kill a 'scalafx' app, when fiber is interrupted, the 'Platform.runLater(Platform.exit())'
  // should happen from the thread the scalafx is running on.
  private var shouldKill = false

  def forkNewWindowApp = for {
    aggregator <- ZIO.service[PerformanceDataAggregator]
    // A simulated fiber which acts as app killer.
    killFiber <- ZIO.never.onInterrupt {
      ZIO.attempt {
        // TODO test this as closing mechanism
        //        window.stage.close()
        shouldKill = true
      }.orDie *> logger.logVerbose("Closing window")
    }.fork
    _ <- (for {
      syncLogger <- logger.toSyncLogger
      maybeError <- ZIO.attempt {
        val window = new PerformanceMonitorWindow(aggregator, syncLogger, killSwitch = () => {
          if (shouldKill) {
            Platform.runLater(Platform.exit())
          }
        })
        syncLogger.logVerbose("Starting window")
        window.main(Array.empty)
        syncLogger.logVerbose("Window finished")
      }.either
      _ <- maybeError match {
        case Right(_) => ZIO.unit
        case Left(err)  => logger.logError(err)
      }
    } yield ()).fork
  } yield killFiber

  def layer(metricSendIntervalMillis: Int) =
    ZLayer.succeed(MetricsConfig(interval = _root_.java.time.Duration.ofMillis(metricSendIntervalMillis)))
    ++ PerformanceDataAggregator.layer
}