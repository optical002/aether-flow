package aetherflow.engine.graphics.opengl.data

import yantl.*

object WindowId extends Newtype.WithoutValidationOf[Long] with StdLibGivens with math.Integral.ExtraImplicits
type WindowId = WindowId.Type

object CompiledShader extends Newtype.WithoutValidationOf[Int] with StdLibGivens with math.Integral.ExtraImplicits
type CompiledShader = CompiledShader.Type

object ShaderSource extends Newtype.WithoutValidationOf[String] with StdLibGivens
type ShaderSource = ShaderSource.Type

object ShaderProgram extends Newtype.WithoutValidationOf[Int] with StdLibGivens with math.Integral.ExtraImplicits
type ShaderProgram = ShaderProgram.Type

object VertexArrayObject extends Newtype.WithoutValidationOf[Int] with StdLibGivens with math.Integral.ExtraImplicits
type VertexArrayObject = VertexArrayObject.Type

object UniformLocation extends Newtype.WithoutValidationOf[Int] with StdLibGivens with math.Integral.ExtraImplicits
type UniformLocation = UniformLocation.Type

