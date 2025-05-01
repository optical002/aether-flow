package engine.performance

object PerformanceMetrics {
  import PerformanceDataAggregator.*

  val startup = "Startup"
  val update = "Update"

  val ecsStartup = Header(startup, "ECS")
  def ecsSystemStartup(sysName: String) = Header(startup, s"ECS system - '$sysName'")
  val windowStartup = Header(startup, "Window startup")

  val fps = Header(update, "FPS")
  val frameDurationPerformance = Header(update, "Frame Duration")

  def frameDuration(name: String) = Header("Computed Single Frame In", name)
  def ecsMetric(name: String) = Header("ECS", name)
  def render(name: String) = Header("Render", name)
  def resources(name: String) = Header("Resources", name)
}
