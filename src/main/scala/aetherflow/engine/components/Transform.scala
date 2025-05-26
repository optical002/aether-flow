package aetherflow.engine.components

import aetherflow.engine.ecs.Component
import aetherflow.engine.graphics.data.Vec3f

case class Transform(
  position: Vec3f = Vec3f(0, 0, 0),
  rotation: Vec3f = Vec3f(0, 0, 0),
  scale: Vec3f = Vec3f(1, 1, 1)
) extends Component {
  def applyVelocity(velocity: Vec3f): Transform = {
    copy(position = position ++ velocity)
  }
}
