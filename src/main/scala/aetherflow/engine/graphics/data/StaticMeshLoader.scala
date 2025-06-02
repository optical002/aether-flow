package aetherflow.engine.graphics.data

import aetherflow.engine.graphics.Utils
import org.lwjgl.assimp.*
import org.lwjgl.assimp.Assimp
import org.lwjgl.assimp.Assimp.*

import scala.collection.*


object StaticMeshLoader {
  /**
   * @param resourcePath The path to the file where the model file is located. This is an absolute path,
   *                     because Assimp may need to load additional files and may use the same base path as
   *                     the resource path (For instance, material files for wavefront, OBJ, files). If you embed
   *                     your resources inside a JAR file, Assimp will not be able to import it, so uts must
   *                     be a file system path.
   * @param texturesDir The path to the directory that will hold the textures for this model. This a
   *                    CLASSPATH relative path. For instance, a wavefront material file may define
   *                    several texture files. The code, expect this files to be located in the
   *                    texturesDir directory. If you find texture loading errors you may need to
   *                    manually tweak these paths in the model file.
   * @return
   */
  def load(resourcePath: String, texturesDir: String): Array[Mesh] = load(
    resourcePath, texturesDir,
    aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals
  )

  def load(resourcePath: String, texturesDir: String, flags: Int): Array[Mesh] = {
    val aiScene = Assimp.aiImportFile(resourcePath, flags)
    if (aiScene == null) {
      throw new RuntimeException("Assimp error: " + Assimp.aiGetErrorString())
    }

    val numMaterials = aiScene.mNumMaterials()
    val aiMaterials = aiScene.mMaterials()
    val textures = mutable.Buffer[Texture]()
    for (i <- 0 until numMaterials) {
      val aiMaterial = AIMaterial.create(aiMaterials.get(i))
      processMaterial(aiMaterial, textures, texturesDir)
    }

    val numMeshes = aiScene.mNumMeshes()
    val aiMeshes = aiScene.mMeshes()
    val meshes = mutable.Buffer[Mesh]()
    for (i <- 0 until numMeshes) {
      val aiMesh = AIMesh.create(aiMeshes.get(i))
      val mesh = processMesh(aiMesh, textures)
      meshes += mesh
    }

    meshes.toArray
  }

  def processMaterial(aiMaterial: AIMaterial, textures: mutable.Buffer[Texture], texturesDir: String): Unit = {
    val color = AIColor4D.create()

    val path = AIString.calloc()
    aiGetMaterialTexture(
      aiMaterial, aiTextureType_DIFFUSE, 0, path, null.asInstanceOf[Array[Int]], null, null, null, null, null
    )
    val textPath = path.dataString()
    val texture = Texture(
      Utils.loadTexture(path = s"$texturesDir/$textPath"),
      Texture.Kind.Diffuse,
      textPath
    )

    textures += texture
  }

  def processMesh(aiMesh: AIMesh, textures: mutable.Buffer[Texture]): Mesh = {
    val mutableVertices = mutable.Buffer[Vertex]()
    val mutableIndices = mutable.Buffer[Short]()
    val mutableNormals = mutable.Buffer[Float]()
    val mutableTextures = mutable.Buffer[Texture]()

    ???
  }
}
