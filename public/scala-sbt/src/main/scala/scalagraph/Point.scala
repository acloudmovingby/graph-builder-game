package scalagraph

import scala.scalajs.js

case class Point(x: Int, y: Int) {
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

