package aetherflow.engine.graphics.data

import aetherflow.engine.graphics.Utils
import org.lwjgl.assimp.*

import scala.collection.*

class Model private(
  private val meshes: Array[Mesh],
  private val directory: String
) {
  def draw(shader: Shader): Unit = meshes.foreach(_.draw(shader))
}
object Model {
  /**
   * Imports a model from a file(e.g. '.fbx'), slow.
   *
   * Use it only to load model to memory, then cache it to file.
   **/
  def importModel(path: String): Model = {
    val mutableMeshes = mutable.Buffer[Mesh]()
    val directory = path.substring(0, path.lastIndexOf("/"))

    def loadMaterialTextures(
      mat: AIMaterial, textureType: Int, textureKind: Texture.Kind
    ): Array[Texture] = {
      val mutableTextures = mutable.Buffer[Texture]()

      for (i <- 0 until Assimp.aiGetMaterialTextureCount(mat, textureType)) {
        val color = AIColor4D.create()

        val path = AIString.calloc()
        Assimp.aiGetMaterialTexture(
          mat, textureType, i, path, null.asInstanceOf[Array[Int]], null, null, null, null, null
        )
        val textPath = path.dataString()
        val textureDirectory = s"$directory/$textPath"
//        println(s"Loading texture: relative path: $textPath, absolute path: $textureDirectory")

        val texture = Texture(
          // Note: This loads same texture multiple times, optimize later.
          Utils.loadTexture(path = textureDirectory),
          textureKind,
          textPath
        )

        mutableTextures += texture
      }

      mutableTextures.toArray
    }

    def processMesh(mesh: AIMesh, scene: AIScene): Mesh = {
      val mutableVertices = mutable.Buffer[Vertex]()
      val mutableIndices = mutable.Buffer[Short]()
      val mutableTextures = mutable.Buffer[Texture]()

      // Processing vertices
      for (i <- 0 until mesh.mNumVertices()) {
        val aiPosition = mesh.mVertices().get(i)
        val position = Vec3f(aiPosition.x(), aiPosition.y(), aiPosition.z())

        val aiNormal = mesh.mNormals().get(i)
        val normal = Vec3f(aiNormal.x(), aiNormal.y(), aiNormal.z())

        val texCoords =
          if (mesh.mTextureCoords(0) != null) {
            val aiTextureCoords = mesh.mTextureCoords(0).get(i)
            Vec2f(aiTextureCoords.x(), aiTextureCoords.y())
          } else {
            Vec2f(0.0f, 0.0f)
          }

        mutableVertices += Vertex(position, normal, texCoords)
      }

      // Processing indices
      for (i <- 0 until mesh.mNumFaces()) {
        val face = mesh.mFaces().get(i)
        for (j <- 0 until face.mNumIndices()) {
          mutableIndices += face.mIndices().get(j).toShort
        }
      }

      // Processing textures
      if (mesh.mMaterialIndex() >= 0) {
        val materialPtr = scene.mMaterials().get(mesh.mMaterialIndex())
        val material = AIMaterial.create(materialPtr)

        val diffuseMaps = loadMaterialTextures(material, Assimp.aiTextureType_DIFFUSE, Texture.Kind.Diffuse)
        val specularMaps = loadMaterialTextures(material, Assimp.aiTextureType_SPECULAR, Texture.Kind.Specular)

        mutableTextures ++= specularMaps ++ diffuseMaps
      }

      Mesh.create(
        mutableVertices.toArray,
        mutableIndices.toArray,
        None //mutableTextures.toArray
      )
    }

    def processNode(node: AINode, scene: AIScene): Unit = {
      for (meshIdx <- 0 until node.mNumMeshes()) {
        val meshPtr = scene.mMeshes().get(meshIdx)
        val mesh = AIMesh.create(meshPtr)
        mutableMeshes += processMesh(mesh, scene)
      }
      for (childNodeIdx <- 0 until node.mNumChildren()) {
        val childNodePtr = node.mChildren().get(childNodeIdx)
        val childNode = AINode.create(childNodePtr)
        processNode(childNode, scene)
      }
    }

    val scene = Assimp.aiImportFile(path,
      Assimp.aiProcess_Triangulate |
      Assimp.aiProcess_FlipUVs |
      Assimp.aiProcess_JoinIdenticalVertices |
      Assimp.aiProcess_FixInfacingNormals |
      Assimp.aiProcess_GenNormals
    )

    if (
      scene == null ||
      (scene.mFlags & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 ||
      scene.mRootNode() == null
    ) {
      throw new RuntimeException("Assimp error: " + Assimp.aiGetErrorString())
    }

    processNode(scene.mRootNode(), scene)

    new Model(mutableMeshes.toArray, directory)
  }

  def importModel_v2(path: String): Model = {
    import org.lwjgl.assimp.Assimp.*

    val mutableMeshes = mutable.Buffer[Mesh]()
    val directory = path.substring(0, path.lastIndexOf("/"))

    val scene = Assimp.aiImportFile(path,
      Assimp.aiProcess_Triangulate |
        Assimp.aiProcess_FlipUVs |
        Assimp.aiProcess_JoinIdenticalVertices |
        Assimp.aiProcess_FixInfacingNormals |
        Assimp.aiProcess_GenNormals
    )

    if (
      scene == null ||
        (scene.mFlags & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 ||
        scene.mRootNode() == null
    ) {
      throw new RuntimeException("Assimp error: " + Assimp.aiGetErrorString())
    }

    val numMaterials = scene.mNumMaterials()
    val aiMaterialsPtr = scene.mMaterials()
    val materials = mutable.Map[Int, Material]()
    for (i <- 0 until numMaterials) {
      val aiMaterialPtr = aiMaterialsPtr.get(i)
      val aiMaterial = AIMaterial.create(aiMaterialPtr)

      println(s"processing material $i from model: $path")
      materials += (i -> Material(
        ambientColor = processColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT),
        diffuseColor = processColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE),
        specularColor = processColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR),
        diffuseTexture = processTexture(aiMaterial, Texture.Kind.Diffuse),
        specularTexture = processTexture(aiMaterial, Texture.Kind.Specular),
        shininess = 32
      ))
    }
    
    def processColor(aiMaterial: AIMaterial, colorKind: String): (Float, Float, Float, Float) = {
      val color = AIColor4D.create()
      val result = aiGetMaterialColor(aiMaterial, colorKind, aiTextureType_NONE, 0, color)
      (color.r, color.g, color.b, color.a)
    }

    def processTexture(
      aiMaterial: AIMaterial, textureKind: Texture.Kind
    ): Option[Texture] = {
      val colour = AIColor4D.create
      val pathStr = AIString.calloc()
      aiGetMaterialTexture(
        aiMaterial, textureKind.asAssimpId, 0, pathStr, null.asInstanceOf[Array[Int]], null, null, null, null, null
      )
      val textPath = pathStr.dataString()

      Option.when(textPath != null && textPath.nonEmpty) {
        Texture(
          Utils.loadTexture(path = s"$directory/$textPath"),
          textureKind,
          textPath
        )
      }
    }

    val numMeshes = scene.mNumMeshes()
    val aiMeshesPtr = scene.mMeshes()
    for (i <- 0 until numMeshes) {
      val aiMeshPtr = aiMeshesPtr.get(i)
      val aiMesh = AIMesh.create(aiMeshPtr)
      val mesh = processMesh(aiMesh, materials.toMap)
      mutableMeshes += mesh
    }

    def processMesh(aiMesh: AIMesh, materials: Map[Int, Material]): Mesh = {
      val mutableVertices = mutable.Buffer[Vertex]()
      val mutableIndices = mutable.Buffer[Short]()
      val mutableTextures = mutable.Buffer[Texture]()

      // Processing vertices
      for (i <- 0 until aiMesh.mNumVertices()) {
        val aiPosition = aiMesh.mVertices().get(i)
        val position = Vec3f(aiPosition.x(), aiPosition.y(), aiPosition.z())

        val aiNormal = aiMesh.mNormals().get(i)
        val normal = Vec3f(aiNormal.x(), aiNormal.y(), aiNormal.z())

        val texCoords =
          if (aiMesh.mTextureCoords(0) != null) {
            val aiTextureCoords = aiMesh.mTextureCoords(0).get(i)
            Vec2f(aiTextureCoords.x(), aiTextureCoords.y())
          } else {
            Vec2f(0.0f, 0.0f)
          }

        mutableVertices += Vertex(position, normal, texCoords)
      }

      // Processing indices
      for (i <- 0 until aiMesh.mNumFaces()) {
        val face = aiMesh.mFaces().get(i)
        for (j <- 0 until face.mNumIndices()) {
          mutableIndices += face.mIndices().get(j).toShort
        }
      }

      Mesh.create(
        mutableVertices.toArray,
        mutableIndices.toArray,
        materials.get(aiMesh.mMaterialIndex())
      )
    }

    new Model(mutableMeshes.toArray, path)
  }
}
































