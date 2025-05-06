package aetherflow.engine.graphics

trait Shader {
  def vertexShader: String
  def fragmentShader: String
  // for now lets tangle a bit with opengl, since i do not know what awaits me with this graphics backend.
  def initialize(shaderProgram: Int): Unit
  def render(): Unit
}
