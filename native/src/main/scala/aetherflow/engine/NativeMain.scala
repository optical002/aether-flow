package aetherflow.engine

import cats.effect.*

object NativeMain extends IOApp.Simple {
  def run = IO.println("Hello Cats Effect!")
}
