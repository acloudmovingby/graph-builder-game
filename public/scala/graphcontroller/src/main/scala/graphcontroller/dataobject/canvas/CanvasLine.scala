package graphcontroller.dataobject.canvas

import scala.scalajs.js

import graphcontroller.dataobject.{Point, PointJS}

/** Represents data necessary to draw a line with the HTML Canvas API */
case class CanvasLine(
	from: Point,
	to: Point,
	width: Int,
	color: String // Hex string, e.g. "#FF0000"
) extends RenderOp {
	def toJS: CanvasLineJS = js.Dynamic.literal(
		from = this.from.toJS,
		to = this.to.toJS,
		width = this.width,
		color = this.color
	).asInstanceOf[CanvasLineJS]
}

/** JS compatible equivalent of CanvasLine */
@js.native
trait CanvasLineJS extends js.Object {
	val from: PointJS
	val to: PointJS
	val width: Int
	val color: String
}