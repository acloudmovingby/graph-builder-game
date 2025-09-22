package scalagraph.dataobject.canvas

import scala.scalajs.js

import scalagraph.dataobject.{Point, PointJS}

case class CanvasLine(
	from: Point,
	to: Point,
	width: Int,
	color: String // Hex string, e.g. "#FF0000"
) {
	def toJS: CanvasLineJS = js.Dynamic.literal(
		from = this.from.toJS,
		to = this.to.toJS,
		width = this.width,
		color = this.color
	).asInstanceOf[CanvasLineJS]
}

@js.native
trait CanvasLineJS extends js.Object {
	val from: PointJS
	val to: PointJS
	val width: Int
	val color: String
}