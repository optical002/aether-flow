package aetherflow.engine.data

import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.utils.DynamicPool

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

/** Column-Major matrix 4x4 for Floats. */
case class Mat4f private (
  m00: Float, m10: Float, m20: Float, m30: Float,
  m01: Float, m11: Float, m21: Float, m31: Float,
  m02: Float, m12: Float, m22: Float, m32: Float,
  m03: Float, m13: Float, m23: Float, m33: Float,
) {
  def load(location: GLint): Unit = {
    val ptr: Ptr[GLfloat] = stackalloc[GLfloat](16)
    ptr(0) = m00; ptr(1) = m10; ptr(2) = m20; ptr(3) = m30
    ptr(4) = m01; ptr(5) = m11; ptr(6) = m21; ptr(7) = m31
    ptr(8) = m02; ptr(9) = m12; ptr(10) = m22; ptr(11) = m32
    ptr(12) = m03; ptr(13) = m13; ptr(14) = m23; ptr(15) = m33
    glUniformMatrix4fv(location, count = 1.toUInt, transpose = GL_FALSE, ptr)
  }
}
object Mat4f {
  private lazy val identity = Mat4f(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1,
  )

  class Builder private (
    private var m00: Float, private var m10: Float, private var m20: Float, private var m30: Float,
    private var m01: Float, private var m11: Float, private var m21: Float, private var m31: Float,
    private var m02: Float, private var m12: Float, private var m22: Float, private var m32: Float,
    private var m03: Float, private var m13: Float, private var m23: Float, private var m33: Float,
  ) {
    def build: Mat4f = {
      Mat4f(
        m00 = m00, m10 = m10, m20 = m20, m30 = m30,
        m01 = m01, m11 = m11, m21 = m21, m31 = m31,
        m02 = m02, m12 = m12, m22 = m22, m32 = m32,
        m03 = m03, m13 = m13, m23 = m23, m33 = m33,
      )
    }

    def loadIdentity: Builder = load(identity)
    def load(m: Mat4f): Builder = {
      m00 = m.m00; m01 = m.m01; m02 = m.m02; m03 = m.m03
      m10 = m.m10; m11 = m.m11; m12 = m.m12; m13 = m.m13
      m20 = m.m20; m21 = m.m21; m22 = m.m22; m23 = m.m23
      m30 = m.m30; m31 = m.m31; m32 = m.m32; m33 = m.m33
      this
    }

//    def viewFromCamera(camera: Camera): Mat4f.Builder =
//      camera.updateViewMatrix(this)

    def lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Mat4f.Builder =
      lookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
    def lookAt(
      eyeX: Float, eyeY: Float, eyeZ: Float,
      centerX: Float, centerY: Float, centerZ: Float,
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      // TODO: Fix Vec3f Heap allocations.
      val f = Vec3f(centerX - eyeX, centerY - eyeY, centerZ - eyeZ).normalize
      val up = Vec3f(upX, upY, upZ).normalize
      val s = f.cross(up).normalize
      val u = s.cross(f)

      m00 = s.x;  m10 = u.x;  m20 = -f.x;  m30 = 0f
      m01 = s.y;  m11 = u.y;  m21 = -f.y;  m31 = 0f
      m02 = s.z;  m12 = u.z;  m22 = -f.z;  m32 = 0f
      m03 = -s.dot(Vec3f(eyeX, eyeY, eyeZ))
      m13 = -u.dot(Vec3f(eyeX, eyeY, eyeZ))
      m23 =  f.dot(Vec3f(eyeX, eyeY, eyeZ))
      m33 = 1f

      this
    }

    def perspective(fov: Float, aspect: Float, near: Float, far: Float): Mat4f.Builder = {
      val tanHalfFov = math.tan(fov / 2).toFloat
      val rangeInv = 1f / (near - far)

      m00 = 1f / (aspect * tanHalfFov)
      m11 = 1f / tanHalfFov
      m22 = (near + far) * rangeInv
      m23 = 2f * near * far * rangeInv
      m32 = -1f

      m01 = 0f; m02 = 0f; m03 = 0f
      m10 = 0f; m12 = 0f; m13 = 0f
      m20 = 0f; m21 = 0f
      m30 = 0f; m31 = 0f; m33 = 0f

      this
    }

    def translate(t: Vec3f): Mat4f.Builder = translate(t.x, t.y, t.z)
    def translate(x: Float, y: Float, z: Float): Mat4f.Builder = {
      m03 += m00 * x + m01 * y + m02 * z
      m13 += m10 * x + m11 * y + m12 * z
      m23 += m20 * x + m21 * y + m22 * z
      m33 += m30 * x + m31 * y + m32 * z
      this
    }

    def rotate(angle: Float, axis: Vec3f): Mat4f.Builder = rotate(angle, axis.x, axis.y, axis.z)
    def rotate(angle: Float, x: Float, y: Float, z: Float): Mat4f.Builder = {
      val c = math.cos(angle).toFloat
      val s = math.sin(angle).toFloat
      val invC = 1f - c
      val len = math.sqrt(x*x + y*y + z*z).toFloat
      val nx = x / len
      val ny = y / len
      val nz = z / len

      val rm00 = c + nx*nx*invC
      val rm01 = nx*ny*invC - nz*s
      val rm02 = nx*nz*invC + ny*s
      val rm10 = ny*nx*invC + nz*s
      val rm11 = c + ny*ny*invC
      val rm12 = ny*nz*invC - nx*s
      val rm20 = nz*nx*invC - ny*s
      val rm21 = nz*ny*invC + nx*s
      val rm22 = c + nz*nz*invC

      val t00 = m00*rm00 + m01*rm10 + m02*rm20
      val t01 = m00*rm01 + m01*rm11 + m02*rm21
      val t02 = m00*rm02 + m01*rm12 + m02*rm22
      val t10 = m10*rm00 + m11*rm10 + m12*rm20
      val t11 = m10*rm01 + m11*rm11 + m12*rm21
      val t12 = m10*rm02 + m11*rm12 + m12*rm22
      val t20 = m20*rm00 + m21*rm10 + m22*rm20
      val t21 = m20*rm01 + m21*rm11 + m22*rm21
      val t22 = m20*rm02 + m21*rm12 + m22*rm22
      val t30 = m30*rm00 + m31*rm10 + m32*rm20
      val t31 = m30*rm01 + m31*rm11 + m32*rm21
      val t32 = m30*rm02 + m31*rm12 + m32*rm22

      m00 = t00; m01 = t01; m02 = t02
      m10 = t10; m11 = t11; m12 = t12
      m20 = t20; m21 = t21; m22 = t22
      m30 = t30; m31 = t31; m32 = t32

      this
    }

    def rotateEuler(v: Vec3f): Mat4f.Builder = rotateEuler(v.x, v.y, v.z)
    def rotateEuler(x: Float, y: Float, z: Float): Mat4f.Builder = {
      rotate(z, 0f, 0f, 1f)
        .rotate(y, 0f, 1f, 0f)
        .rotate(x, 1f, 0f, 0f)
    }

    def scale(s: Vec3f): Mat4f.Builder = scale(s.x, s.y, s.z)
    def scale(s: Float): Mat4f.Builder = scale(s, s, s)
    def scale(x: Float, y: Float, z: Float): Mat4f.Builder = {
      m00 *= x; m01 *= y; m02 *= z
      m10 *= x; m11 *= y; m12 *= z
      m20 *= x; m21 *= y; m22 *= z
      m30 *= x; m31 *= y; m32 *= z
      this
    }
  }
  object Builder {
    lazy val instances = DynamicPool.create[Builder](
      initialSize = 16,
      create = new Builder(
        m00 = 0, m10 = 0, m20 = 0, m30 = 0,
        m01 = 0, m11 = 0, m21 = 0, m31 = 0,
        m02 = 0, m12 = 0, m22 = 0, m32 = 0,
        m03 = 0, m13 = 0, m23 = 0, m33 = 0,
      )
    )
  }
}



