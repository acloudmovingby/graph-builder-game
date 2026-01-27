package graphcontroller.dataobject

import scala.scalajs.js

case class Vector2D(x: Int, y: Int) extends Shape {
	type This = Vector2D

	def translate(vec2: Vector2D) = Vector2D(x + vec2.x, y + vec2.y)

	def scale(scaleFactor: Int): Vector2D = Vector2D(x * scaleFactor, y * scaleFactor)

	def rotate(radians: Double): Vector2D = {
		val rotateMatrix = Array(
			Array(math.cos(radians), -math.sin(radians)),
			Array(math.sin(radians), math.cos(radians))
		)
		Vector2D(
			(x * rotateMatrix(0)(0) + y * rotateMatrix(0)(1)).toInt,
			(x * rotateMatrix(1)(0) + y * rotateMatrix(1)(1)).toInt
		)
	}

	def toJS: PointJS = js.Dynamic.literal(
		x = this.x,
		y = this.y
	).asInstanceOf[PointJS]
}

@js.native
trait PointJS extends js.Object {
	val x: Int
	val y: Int
}

