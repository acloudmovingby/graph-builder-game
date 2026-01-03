package graphcontroller.dataobject.canvas

import scala.scalajs.js

import graphcontroller.dataobject.{Triangle, TriangleJS}

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class TriangleCanvas(
	 tri: Triangle,
	 color: String // Hex string, e.g. "#FF0000"
) extends RenderOp {
	def toJS: TriangleCanvasJS = js.Dynamic.literal(
		tri = this.tri.toJS,
		color = this.color
	).asInstanceOf[TriangleCanvasJS]
}

/** JS compatible equivalent of TriangleCanvas */
@js.native
trait TriangleCanvasJS extends js.Object {
	val tri: TriangleJS
	val color: String
}