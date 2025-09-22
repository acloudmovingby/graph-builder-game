package scalagraph.dataobject

import scala.scalajs.js

case class Point(x: Int, y: Int) {
	def scaled(scaleFactor: Int): Point = Point(x * scaleFactor, y * scaleFactor)

	def rotate(radians: Double): Point = {
		val rotateMatrix = Array(
			Array(math.cos(radians), -math.sin(radians)),
			Array(math.sin(radians), math.cos(radians))
		)
		Point(
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

