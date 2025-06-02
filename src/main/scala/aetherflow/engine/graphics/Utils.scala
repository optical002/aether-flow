package aetherflow.engine.graphics

import org.joml.*
import org.lwjgl.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL14.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*

object Utils {
  def loadTexture(path: String): Int = {
    val stack = MemoryStack.stackPush()
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    val channels = stack.mallocInt(1)

    stbi_set_flip_vertically_on_load(true)
    val image = stbi_load(path, width, height, channels, 4) // Force RGBA
    if (image == null) {
      throw new RuntimeException(s"Failed to load image at path ($path): ${stbi_failure_reason()}")
    }

    val textureId = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, textureId)
    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA,
      width.get(0),
      height.get(0),
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      image
    )
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glGenerateMipmap(GL_TEXTURE_2D)

    glBindTexture(GL_TEXTURE_2D, 0)
    stbi_image_free(image)
    stack.close()

    println(s"Loaded texture($path): $textureId")
    textureId
  }
}
