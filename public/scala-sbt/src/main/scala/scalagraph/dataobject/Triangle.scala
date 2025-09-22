package scalagraph.dataobject

import scalagraph.dataobject.{Point, PointJS}

import scala.scalajs.js

case class Triangle(
	pt1: Point,
	pt2: Point,
	pt3: Point
) {
	def scaled(scaleFactor: Int): Triangle =
		Triangle(pt1.scaled(scaleFactor), pt2.scaled(scaleFactor), pt3.scaled(scaleFactor))

	def toJS: TriangleJS = js.Dynamic.literal(
		pt1 = this.pt1.toJS,
		pt2 = this.pt2.toJS,
		pt3 = this.pt3.toJS
	).asInstanceOf[TriangleJS]
}

@js.native
trait TriangleJS extends js.Object {
	val pt1: PointJS
	val pt2: PointJS
	val pt3: PointJS
}
