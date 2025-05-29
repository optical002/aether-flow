package aetherflow.engine.graphics.data

import aetherflow.engine.graphics.data.{Mat4f, Vec3f}
import org.joml.Math.*
import org.lwjgl.glfw.GLFW.*

class Camera private(
  val config: Camera.Config,
  private var position: Vec3f = Vec3f(0, 0, 0),
  private var front: Vec3f = Vec3f(0, 0, -1),
  private var right: Vec3f = Vec3f(1, 0, 0),
  private var up: Vec3f = Vec3f(0, 1, 0),
  private var yaw: Float = -90.0f,
  private var pitch: Float = 0,
  private var zoom: Float = 45.0f,
) {
  def getZoom: Float = zoom
  def getPosition: Vec3f = position
  def getFront: Vec3f = front

  def updateViewMatrix(mat: Mat4f.Builder): Mat4f.Builder =
    mat.lookAt(position, position ++ front, up)

  def processInput(window: Long): Unit = {
    val cameraSpeed = config.movementSpeed
    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
      position ++= front * cameraSpeed
    }
    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
      position --= front * cameraSpeed
    }
    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
      position --= right * cameraSpeed
    }
    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
      position ++= right * cameraSpeed
    }
  }

  private def updateCameraVectors(): Unit = {
    def cameraLook(yaw: Float, pitch: Float): Vec3f = Vec3f(
      x = cos(toRadians(yaw)) * cos(toRadians(pitch)),
      y = sin(toRadians(pitch)),
      z = sin(toRadians(yaw)) * cos(toRadians(pitch)),
    )

    front = cameraLook(yaw, pitch).normalize
    right = front.cross(config.worldUp).normalize
    up = right.cross(front).normalize
  }
}
object Camera {
  def create(position: Vec3f, config: Camera.Config, window: Long): Camera = {
    val camera = new Camera(config = config, position = position)
    camera.updateCameraVectors()

    var firstMouse = true
    var lastX = 0.0f
    var lastY = 0.0f
    glfwSetCursorPosCallback(window, (_, xPos, yPos) => {
      if (firstMouse) {
        lastX = xPos.toFloat
        lastY = yPos.toFloat
        firstMouse = false
      }

      var xOffset: Float = (xPos - lastX).toFloat
      var yOffset: Float = (lastY - yPos).toFloat
      lastX = xPos.toFloat
      lastY = yPos.toFloat

      val sensitivity = 0.1f
      xOffset *= sensitivity
      yOffset *= sensitivity

      camera.yaw += xOffset
      camera.pitch += yOffset

      if (camera.pitch > 89.0f) camera.pitch = 89.0f
      if (camera.pitch < -89.0f) camera.pitch = -89.0f

      camera.updateCameraVectors()
    })

    glfwSetScrollCallback(window, (_, _, yOffset) => {
      camera.zoom -= yOffset.toFloat
      if (camera.zoom < 1.0f) camera.zoom = 1.0f
      if (camera.zoom > 45.0f) camera.zoom = 45.0f
    })
    camera
  }

  case class Config(
    worldUp: Vec3f = Vec3f.up,
    movementSpeed: Float = 2.5f,
    mouseSensitivity: Float = 0.1f,
  )
}
