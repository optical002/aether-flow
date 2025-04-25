package engine.os.executors

import engine.core.Logger
import engine.os.ShellExecutor
import engine.os.powershell.PowershellScript
import zio.stream.*
import zio.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object PowershellExecutor extends ShellExecutor[PowershellScript] {
  lazy val eofSymbol = "__EOF__"

  def launchInSeparateAdminProcess(outputFilePath: Path, script: String): Task[Unit] = ZIO.attempt {
    val scriptPath = Files.createTempFile("script-", ".ps1")
    Files.write(scriptPath, script.getBytes(StandardCharsets.UTF_8))

    val wrappedScriptPath = Files.createTempFile("admin-wrapper-", ".ps1")
    val wrappedScriptContent =
      s"""
         |powershell -ExecutionPolicy Bypass -NoProfile -File "${scriptPath.toAbsolutePath}" | Out-File -FilePath "${outputFilePath.toAbsolutePath}" -Encoding utf8
         |"$eofSymbol" | Out-File -FilePath "${outputFilePath.toAbsolutePath}" -Encoding utf8 -Append
         |""".stripMargin
    Files.write(wrappedScriptPath, wrappedScriptContent.getBytes(StandardCharsets.UTF_8))

    val command: Seq[String] = Seq(
      "powershell",
      "-Command",
      s"""Start-Process powershell.exe -ArgumentList '-File "${wrappedScriptPath.toAbsolutePath}"' -WindowStyle Hidden""" // Note add ' -Verb RunAs' to run as admin
    )

    val processBuilder = new ProcessBuilder(command.asJava)
    processBuilder.inheritIO()
    processBuilder.start()
  }

  def readNewLines(startAt: Int, filePath: Path, logger: Logger): Task[(List[String], Int)] = for {
    lines <- ZIO.attempt(Files.readAllLines(filePath, StandardCharsets.UTF_8).asScala.toList.map(_.stripPrefix("\uFEFF")))
    newLines = lines.drop(startAt)
    _ <- logger.logVerbose(s"Read additional content: $newLines")
  } yield (newLines, lines.length)

  case class FileState(lastIndex: Int, buffer: List[String])

  def streamFile(
    filePath: Path, logger: Logger
  ): ZStream[Any, Throwable, String] = ZStream.unfoldZIO(FileState(0, Nil)) {
      case FileState(idx, Nil) => for {
        (lines, newIdx) <- readNewLines(idx, filePath, logger)
        result <- lines match {
          case Nil => ZIO.succeed(None)
          case ::(head, tail)   => ZIO.succeed(Some(head, FileState(newIdx, tail)))
        }
      } yield result


      case FileState(idx, ::(head, tail)) => for {
        result <- ZIO.succeed(Some(head, FileState(idx, tail)))
        _ <- logger.logVerbose(s"Ingesting by line: '$result'")
      } yield result
    }.repeat(Schedule.spaced(500.millis))

  def program(
    script: String, logger: Logger
  ): Task[Seq[String]] = for {
    _ <- logger.logVerbose("Creating temporary file for storing output")
    outputFilePath <- ZIO.attempt(Files.createTempFile("powershell-output", ".txt"))
    _ <- logger.logVerbose(s"Launching powershell detached process with admin rights")
    _ <- launchInSeparateAdminProcess(outputFilePath, script)
    _ <- logger.logVerbose(s"Streaming temporary output file")
    output <- streamFile(outputFilePath, logger)
      .run(ZSink.collectAllUntil(_ == eofSymbol))
    _ <- logger.logVerbose(s"Powershell script finished")
  } yield output

  override def process(
    script: PowershellScript, logger: Logger
  ): UIO[Fiber[Throwable, Seq[String]]] =
    program(script.get, logger.scope("Powershell-Executor"))
      .map(_.filter(str => str.nonEmpty && str != eofSymbol))
      .fork
}