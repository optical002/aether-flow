package aetherflow.engine.graphics.data

import org.lwjgl.assimp.Assimp

case class Texture(id: Int, kind: Texture.Kind, path: String)
object Texture {
  enum Kind {
    case Diffuse
    case Specular
    
    lazy val asAssimpId = this match {
      case Kind.Diffuse => Assimp.aiTextureType_DIFFUSE
      case Kind.Specular => Assimp.aiTextureType_SPECULAR
    }
  }
}
