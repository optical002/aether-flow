package aetherflow.engine.graphics.data

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*

case class Material(
  ambientColor: (Float, Float, Float, Float),
  diffuseColor: (Float, Float, Float, Float),
  specularColor: (Float, Float, Float, Float),
  diffuseTexture: Option[Texture],
  specularTexture: Option[Texture],
  shininess: Float,
) {
  def apply(shader: Shader): Unit = {
    shader.setUniform3f("material.ambientColor", ambientColor._1, ambientColor._2, ambientColor._3)
    shader.setUniform3f("material.diffuseColor", diffuseColor._1, diffuseColor._2, diffuseColor._3)
    shader.setUniform3f("material.specularColor", specularColor._1, specularColor._2, specularColor._3)
    shader.setFloat("material.shininess", shininess)

    diffuseTexture match {
      case Some(diffuse) =>
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, diffuse.id)
        shader.setFloat("material.texture_diffuse", diffuse.id)
      case None => ()
    }

    specularTexture match {
      case Some(specular) =>
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, specular.id)
        shader.setFloat("material.texture_specular", specular.id)
      case None => ()
    }
  }
}
