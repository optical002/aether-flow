package engine.os

import engine.core.Logger
import zio.*

trait ScriptPipeline[Data] {
  lazy val pipelineName = this.getClass.getSimpleName

  def startupScripts(data: Data): Seq[ShellRunnableScript[?]]
  def closeScripts(data: Data): Seq[ShellRunnableScript[?]]
  
  def createData: Task[Data]
  
  def run(logger: Logger): RIO[Scope, Boolean] = for {
    data <- createData
    result <- ZIO.acquireRelease(
      runScripts(startupScripts(data), logger)
      )(_ => runScripts(closeScripts(data), logger).orDie)
  } yield result


  private def runScripts(
    scripts: Seq[ShellRunnableScript[?]], logger: Logger
  ) = {
    val pipelineLogger = logger.scope(s"Pipeline-$pipelineName")

    for {
      result <- ZIO.foldLeft(scripts)(true) { case (success, script) =>
        val scriptName = script.getClass.getName
        if (success) {
          script.run(pipelineLogger).flatMap {
            case true  =>
              pipelineLogger.logVerbose(s"✅ Script succeeded - $scriptName")
                *> ZIO.succeed(true)
            case false =>
              pipelineLogger.logError(s"❌ Script failed - $scriptName")
                *> ZIO.succeed(false)
          }
        } else {
          pipelineLogger.logError(s"⚠️ Skipped script - $scriptName")
            *> ZIO.succeed(false)
        }
      }
      _ <- logger.logVerbose(s"✅ Pipeline ${this.getClass.getName} completed.")
    } yield result
  }
}
