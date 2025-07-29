package aetherflow.engine.graphics.data

object GraphicsMath {
  def fma(a: Float, b: Float, c: Float): Float = a * b + c
  def invsqrt(r: Float): Float = 1.0F / java.lang.Math.sqrt(r.toDouble).toFloat
}
