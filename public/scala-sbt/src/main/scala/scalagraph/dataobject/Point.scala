package scalagraph.dataobject

import scala.scalajs.js

case class Point(x: Int, y: Int) {
	def scaled(scaleFactor: Int): Point = Point(x * scaleFactor, y * scaleFactor)
	
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

