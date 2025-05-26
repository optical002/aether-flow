package aetherflow.math

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
    private var m00: Float,
    private var m01: Float,
    private var m02: Float,
    private var m03: Float,
    private var m10: Float,
    private var m11: Float,
    private var m12: Float,
    private var m13: Float,
    private var m20: Float,
    private var m21: Float,
    private var m22: Float,
    private var m23: Float,
    private var m30: Float,
    private var m31: Float,
    private var m32: Float,
    private var m33: Float,
    private var properties: Int,
  ) {
    def fill(fb: FloatBuffer): FloatBuffer = {
      fb.put(0, this.m00).put(1, this.m01).put(2, this.m02).put(3, this.m03).put(4, this.m10).put(5, this.m11)
        .put(6, this.m12).put(7, this.m13).put(8, this.m20).put(9, this.m21).put(10, this.m22).put(11, this.m23)
        .put(12, this.m30).put(13, this.m31).put(14, this.m32).put(15, this.m33)
      fb
    }
    
    def load(m: Mat4f): Builder = {
      this.m00 = m.m00
      this.m01 = m.m01
      this.m02 = m.m02
      this.m03 = m.m03
      this.m10 = m.m10
      this.m11 = m.m11
      this.m12 = m.m12
      this.m13 = m.m13
      this.m20 = m.m20
      this.m21 = m.m21
      this.m22 = m.m22
      this.m23 = m.m23
      this.m30 = m.m30
      this.m31 = m.m31
      this.m32 = m.m32
      this.m33 = m.m33
      this.properties = m.properties
      this
    }
    
    private def mutate(
      m00: Float, m10: Float, m20: Float, m30: Float,
      m01: Float, m11: Float, m21: Float, m31: Float,
      m02: Float, m12: Float, m22: Float, m32: Float,
      m03: Float, m13: Float, m23: Float, m33: Float,
      properties: Int
    ): Builder = {
      this.m00 = m00
      this.m10 = m10
      this.m20 = m20
      this.m30 = m30
      this.m01 = m01
      this.m11 = m11
      this.m21 = m21
      this.m31 = m31
      this.m02 = m02
      this.m12 = m12
      this.m22 = m22
      this.m32 = m32
      this.m03 = m03
      this.m13 = m13
      this.m23 = m23
      this.m33 = m33
      this.properties = properties
      this
    }
    
    def lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Mat4f.Builder = 
      lookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
    
    def lookAt(
      eyeX: Float, eyeY: Float, eyeZ: Float, 
      centerX: Float, centerY: Float, centerZ: Float, 
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      if ((this.properties & 4) != 0) {
        setLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
      } else if ((this.properties & 1) != 0){
        lookAtPerspective(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
      } else {
        lookAtGeneric(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
      }
    }
    
    private def lookAtGeneric(
      eyeX: Float, eyeY: Float, eyeZ: Float,
      centerX: Float, centerY: Float, centerZ: Float,
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      var dirX = eyeX - centerX
      var dirY = eyeY - centerY
      var dirZ = eyeZ - centerZ
      val invDirLength = org.joml.Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
      dirX *= invDirLength
      dirY *= invDirLength
      dirZ *= invDirLength
      var leftX = upY * dirZ - upZ * dirY
      var leftY = upZ * dirX - upX * dirZ
      var leftZ = upX * dirY - upY * dirX
      val invLeftLength = org.joml.Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
      leftX *= invLeftLength
      leftY *= invLeftLength
      leftZ *= invLeftLength
      val upnX = dirY * leftZ - dirZ * leftY
      val upnY = dirZ * leftX - dirX * leftZ
      val upnZ = dirX * leftY - dirY * leftX
      val rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ)
      val rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ)
      val rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ)
      val nm00 = this.m00 * leftX + this.m10 * upnX + this.m20 * dirX
      val nm01 = this.m01 * leftX + this.m11 * upnX + this.m21 * dirX
      val nm02 = this.m02 * leftX + this.m12 * upnX + this.m22 * dirX
      val nm03 = this.m03 * leftX + this.m13 * upnX + this.m23 * dirX
      val nm10 = this.m00 * leftY + this.m10 * upnY + this.m20 * dirY
      val nm11 = this.m01 * leftY + this.m11 * upnY + this.m21 * dirY
      val nm12 = this.m02 * leftY + this.m12 * upnY + this.m22 * dirY
      val nm13 = this.m03 * leftY + this.m13 * upnY + this.m23 * dirY
      mutate(
        m00 = nm00, m01 = nm01, m02 = nm02, m03 = nm03,
        m10 = nm10, m11 = nm11, m12 = nm12, m13 = nm13,
        
        m20 = this.m00 * leftZ + this.m10 * upnZ + this.m20 * dirZ,
        m21 = this.m01 * leftZ + this.m11 * upnZ + this.m21 * dirZ,
        m22 = this.m02 * leftZ + this.m12 * upnZ + this.m22 * dirZ,
        m23 = this.m03 * leftZ + this.m13 * upnZ + this.m23 * dirZ,
        
        m30 = this.m00 * rm30 + this.m10 * rm31 + this.m20 * rm32 + this.m30,
        m31 = this.m01 * rm30 + this.m11 * rm31 + this.m21 * rm32 + this.m31,
        m32 = this.m02 * rm30 + this.m12 * rm31 + this.m22 * rm32 + this.m32,
        m33 = this.m03 * rm30 + this.m13 * rm31 + this.m23 * rm32 + this.m33,
        
        properties = this.properties & -14
      )
    }
    
    private def lookAtPerspective(
      eyeX: Float, eyeY: Float, eyeZ: Float,
      centerX: Float, centerY: Float, centerZ: Float,
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      var dirX = eyeX - centerX
      var dirY = eyeY - centerY
      var dirZ = eyeZ - centerZ
      val invDirLength = org.joml.Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
      dirX *= invDirLength
      dirY *= invDirLength
      dirZ *= invDirLength
      var leftX = upY * dirZ - upZ * dirY
      var leftY = upZ * dirX - upX * dirZ
      var leftZ = upX * dirY - upY * dirX
      val invLeftLength = org.joml.Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
      leftX *= invLeftLength
      leftY *= invLeftLength
      leftZ *= invLeftLength
      val upnX = dirY * leftZ - dirZ * leftY
      val upnY = dirZ * leftX - dirX * leftZ
      val upnZ = dirX * leftY - dirY * leftX
      val rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ)
      val rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ)
      val rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ)
      val nm10 = this.m00 * leftY
      val nm20 = this.m00 * leftZ
      val nm21 = this.m11 * upnZ
      val nm30 = this.m00 * rm30
      val nm31 = this.m11 * rm31
      val nm32 = this.m22 * rm32 + this.m32
      val nm33 = this.m23 * rm32
      mutate(
        this.m00 * leftX, this.m11 * upnX, this.m22 * dirX, this.m23 * dirX,
        nm10, this.m11 * upnY, this.m22 * dirY, this.m23 * dirY,
        nm20, nm21, this.m22 * dirZ, this.m23 * dirZ,
        nm30, nm31, nm32, nm33,
        0
      )
    }
    
    private def setLookAt(
      eyeX: Float, eyeY: Float, eyeZ: Float,
      centerX: Float, centerY: Float, centerZ: Float,
      upX: Float, upY: Float, upZ: Float
    ): Mat4f.Builder = {
      var dirX = eyeX - centerX
      var dirY = eyeY - centerY
      var dirZ = eyeZ - centerZ
      val invDirLength = org.joml.Math.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
      dirX *= invDirLength
      dirY *= invDirLength
      dirZ *= invDirLength
      var leftX = upY * dirZ - upZ * dirY
      var leftY = upZ * dirX - upX * dirZ
      var leftZ = upX * dirY - upY * dirX
      val invLeftLength = org.joml.Math.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
      leftX *= invLeftLength
      leftY *= invLeftLength
      leftZ *= invLeftLength
      val upnX = dirY * leftZ - dirZ * leftY
      val upnY = dirZ * leftX - dirX * leftZ
      val upnZ = dirX * leftY - dirY * leftX
      mutate(
        m00 = leftX, m01 = upnX, m02 = dirX, m03 = 0.0F,
        m10 = leftY, m11 = upnY, m12 = dirY, m13 = 0.0F,
        m20 = leftZ, m21 = upnZ, m22 = dirZ, m23 = 0.0F,
        m30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ),
        m31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ),
        m32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ),
        m33 = 1.0F,
        properties = 18
      )
    }
  }
  object Builder {
    private lazy val _instance = new Builder(
      identity.m00, identity.m01, identity.m02, identity.m03,
      identity.m10, identity.m11, identity.m12, identity.m13,
      identity.m20, identity.m21, identity.m22, identity.m23,
      identity.m30, identity.m31, identity.m32, identity.m33,
      identity.properties
    )
    
    def takeIdentityInstance: Builder = _instance.load(identity)
  }
}



