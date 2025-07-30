package aetherflow.engine.graphics.data

import aetherflow.engine.graphics.GMath

// Note operations on vector allocate
case class Vec3f(x: Float, y: Float, z: Float) {
  def ++(v: Vec3f): Vec3f = Vec3f(x + v.x, y + v.y, z + v.z)
  def --(v: Vec3f): Vec3f = Vec3f(x - v.x, y - v.y, z - v.z)

  def *(scalar: Float): Vec3f = Vec3f(x * scalar, y * scalar, z * scalar)
  def *(v: Vec3f): Vec3f = Vec3f(x * v.x, y * v.y, z * v.z)

  lazy val normalize: Vec3f = Vec3f.normalize(this)

  def cross(v: Vec3f): Vec3f = Vec3f.cross(this, v)
  def dot(v: Vec3f): Float = Vec3f.dot(this, v)
}
object Vec3f {
  lazy val sizeOf = 3 * 4
  
  val zero = Vec3f(0.0f, 0.0f, 0.0f)
  val one = Vec3f(1.0f, 1.0f, 1.0f)
  val up = Vec3f(0.0f, 1.0f, 0.0f)
  val down = Vec3f(0.0f, -1.0f, 0.0f)
  val right = Vec3f(1.0f, 0.0f, 0.0f)
  val left = Vec3f(-1.0f, 0.0f, 0.0f)
  val forward = Vec3f(0.0f, 0.0f, 1.0f)
  val back = Vec3f(0.0f, 0.0f, -1.0f)
  val axisX = Vec3f(1, 0, 0)
  val axisY = Vec3f(0, 1, 0)
  val axisZ = Vec3f(0, 0, 1)
  
  def apply(uni: Float): Vec3f = Vec3f(uni, uni, uni)
  def apply(vec2f: Vec2f, z: Float): Vec3f = Vec3f(vec2f.x, vec2f.y, z)
  
  def cross(v1: Vec3f, v2: Vec3f): Vec3f = {
    val x = GMath.fma(v1.y, v2.z, -v1.z * v2.y)
    val y = GMath.fma(v1.z, v2.x, -v1.x * v2.z)
    val z = GMath.fma(v1.x, v2.y, -v1.y * v2.x)
    Vec3f(x, y, z)
  }
  
  def dot(v1: Vec3f, v2: Vec3f): Float = 
    GMath.fma(v1.x, v2.x, GMath.fma(v1.y, v2.y, v1.z * v2.z))
  
  def normalize(v: Vec3f): Vec3f = 
    v * GMath.invsqrt(GMath.fma(v.x, v.x, GMath.fma(v.y, v.y, v.z * v.z)))
}
  
