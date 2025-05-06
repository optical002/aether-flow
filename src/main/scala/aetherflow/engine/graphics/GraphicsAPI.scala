package aetherflow.engine.graphics

import aetherflow.engine.graphics.config.WindowConfig
import zio.*
import zio.stm.*

import scala.collection.*
import scala.util.*

trait GraphicsAPI {
  def init(): Unit
  def close(): Unit
  def createWindow(cfg: WindowConfig): WindowHandle
  def createInputSystem(): InputSystem
  def loadAsset(graphicAsset: GraphicAsset): GraphicHandle
}
