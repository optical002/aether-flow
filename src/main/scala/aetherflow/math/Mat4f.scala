package aetherflow.math

import aetherflow.renderEngine.Camera
import org.joml.*

import java.nio.FloatBuffer

case class Mat4f private (
  m00: Float, m10: Float, m20: Float, m30: Float,
  m01: Float, m11: Float, m21: Float, m31: Float,
  m02: Float, m12: Float, m22: Float, m32: Float,
  m03: Float, m13: Float, m23: Float, m33: Float,
  properties: Int
)
object Mat4f {
  private lazy val identity = Mat4f(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1,
    properties = 30
  )

  class Builder private (
    private val mat: Matrix4f = new Matrix4f()
  ) {
    def fill(fb: FloatBuffer): FloatBuffer = {
      mat.get(fb)
      fb
    }
    
    def loadIdentity: Builder = load(identity)
    def load(m: Mat4f): Builder = {
      mat.m00(m.m00)
      mat.m01(m.m01)
      mat.m02(m.m02)
      mat.m03(m.m03)
      mat.m10(m.m10)
      mat.m11(m.m11)
      mat.m12(m.m12)
      mat.m13(m.m13)
      mat.m20(m.m20)
      mat.m21(m.m21)
      mat.m22(m.m22)
      mat.m23(m.m23)
      mat.m30(m.m30)
      mat.m31(m.m31)
      mat.m32(m.m32)
      mat.m33(m.m33)
      mat.assume(m.properties)
      this
    }
    
    def viewFromCamera(camera: Camera): Mat4f.Builder =
      camera.updateViewMatrix(this)
    
    def lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Mat4f.Builder = 
      lookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
    def lookAt(
      eyeX: Float, eyeY: Float, eyeZ: Float, 
      centerX: Float, centerY: Float, centerZ: Float, 
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      mat.lookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
      this
    }
    
    def perspective(fov: Float, aspect: Float, near: Float, far: Float): Mat4f.Builder = {
      mat.perspective(fov, aspect, near, far)
      this
    }
    
    def translate(t: Vec3f): Mat4f.Builder = translate(t.x, t.y, t.z)
    def translate(x: Float, y: Float, z: Float): Mat4f.Builder = {
      mat.translate(x, y, z)
      this
    }
    
    def rotate(angle: Float, axis: Vec3f): Mat4f.Builder = rotate(angle, axis.x, axis.y, axis.z)
    def rotate(angle: Float, x: Float, y: Float, z: Float): Mat4f.Builder = {
      mat.rotate(angle, x, y, z)
      this
    }
    
    def scale(s: Vec3f): Mat4f.Builder = scale(s.x, s.y, s.z)
    def scale(x: Float, y: Float, z: Float): Mat4f.Builder = {
      mat.scale(x, y, z)
      this
    }
  }
  object Builder {
    lazy val instances = DynamicPool.create[Builder](
      initialSize = 16,
      create = new Builder()
    )
  }
}



