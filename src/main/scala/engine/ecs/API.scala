package engine.ecs

// TODO remove after testing.
case class Transform(x: Float, y: Float) extends Component {
  def applyVelocity(velocity: Velocity): Transform =
    copy(x = x + velocity.dx, y = y + velocity.dy)
}
case class Velocity(dx: Float, dy: Float) extends Component
