package engine.graphics

trait WindowHandle {
  def isActive: Boolean
  def close(): Unit
  def clearScreen(): Unit
  def swapBuffers(): Unit
}
