package aetherflow.engine.graphics.data

import aetherflow.engine.nativelink.gl.GL.*
import aetherflow.engine.nativelink.gl.GLExtras.*
import aetherflow.engine.nativelink.stb_image.{stbi_image_free, stbi_load}
import aetherflow.engine.utils.Resources
import aetherflow.engine.syntax.*

import scalanative.unsafe.*
import scalanative.unsigned.*

class Texture2D private(val texture: GLuint) {
  def bind(): Unit = glBindTexture(GL_TEXTURE_2D, texture)
}
object Texture2D {
  def load(path: String): Texture2D = Zone.acquire { implicit _ =>
    val width = stackalloc[CInt]()
    val height = stackalloc[CInt]()
    val channels = stackalloc[CInt]()
    val data = stbi_load(Resources.getPath("textures/box.png").toString.asCString, width, height, channels, 0)

    val texture = stackalloc[GLuint]()
    glGenTextures(1.toUInt, texture)

    glBindTexture(GL_TEXTURE_2D, !texture)
    glTexImage2D(
      target = GL_TEXTURE_2D,
      level = 0,
      internalFormat = GL_RGBA.toInt,
      width = (!width).toUInt,
      height = (!height).toUInt,
      border = 0,
      format = GL_RGBA,
      type_ = GL_UNSIGNED_BYTE,
      pixels = data
    )
    glGenerateMipmap(GL_TEXTURE_2D)
    stbi_image_free(data)

    new Texture2D(!texture)
  }
}
