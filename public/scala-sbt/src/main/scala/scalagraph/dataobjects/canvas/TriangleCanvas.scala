package scalagraph.dataobjects.canvas

import scala.scalajs.js

import scalagraph.{Point, PointJS}

case class TriangleCanvas(
	 pt1: Point,
	 pt2: Point,
	 pt3: Point,
	 color: String // Hex string, e.g. "#FF0000"
) {
	def toJS: TriangleCanvasJS = js.Dynamic.literal(
		pt1 = this.pt1.toJS,
		pt2 = this.pt2.toJS,
		pt3 = this.pt3.toJS,
		color = this.color
	).asInstanceOf[TriangleCanvasJS]
}

@js.native
trait TriangleCanvasJS extends js.Object {
	val pt1: PointJS
	val pt2: PointJS
	val pt3: PointJS
	val color: String
}