package aetherflow.engine.graphics

import aetherflow.engine.ecs.EcsStateMachine
import aetherflow.engine.graphics.config.WindowConfig
import aetherflow.engine.graphics.data.{Camera, Mat4f}
import zio.{Task, UIO}

trait WindowHandle {
  def isActive: Boolean
  def close(): Unit
  def clearScreen(): Unit
  def swapBuffers(): Unit
  def processInput(): Unit
  def initializeRenderers(ecsStateMachine: EcsStateMachine): UIO[Unit]
  def renderRenderers(
    ecsStateMachine: EcsStateMachine,
    windowConfig: WindowConfig,
  ): Task[Unit]
}
