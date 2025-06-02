package aetherflow.engine.graphics.data

case class Vec2f(x: Float, y: Float) {
  def ++(v: Vec2f): Vec2f = Vec2f(x + v.x, y + v.y)
  def --(v: Vec2f): Vec2f = Vec2f(x - v.x, y - v.y)
  
  def *(scalar: Float): Vec2f = Vec2f(x * scalar, y * scalar)
  def *(v: Vec2f): Vec2f = Vec2f(x * v.x, y * v.y)
}
object Vec2f {
  lazy val sizeOf = 2 * 4
  
  val zero = Vec2f(0.0f, 0.0f)
  val one = Vec2f(1.0f, 1.0f)
  
  def apply(uni: Float): Vec2f = Vec2f(uni, uni)
  def apply(vec3f: Vec3f): Vec2f = Vec2f(vec3f.x, vec3f.y)
}
