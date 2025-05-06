package aetherflow.engine.components

import org.joml.*

class Transform {
  private var position: Vector3f = new Vector3f(0)
  private var rotation: Float = 0
  private var scale: Vector3f = new Vector3f(1)

  def moveBy(by: Vector3f): Unit = position.add(by)
  def rotateBy(by: Float): Unit = rotation += by
  def setScale(as: Float): Unit = scale = new Vector3f(as)
  def scaleBy(by: Float): Unit = scaleBy(scale.mul(by))
  def scaleBy(by: Vector3f): Unit = scale.add(by)

  def transformMatrix(): Matrix4f =
    new Matrix4f()
      .identity()
      .translate(position)
      .rotateZ(rotation)
      .scale(scale)
}