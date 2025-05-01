package engine.graphics

trait GraphicHandle {
  def unload(): Unit
  def render(): Unit
}
