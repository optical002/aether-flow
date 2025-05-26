package aetherflow.engine.graphics

import aetherflow.engine.graphics.config.WindowConfig

trait GraphicsAPI {
  def init(): Unit
  def close(): Unit
  def createWindow(cfg: WindowConfig): WindowHandle
}
