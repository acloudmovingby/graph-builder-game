package graphcontroller.dataobject

import graphcontroller.dataobject.{Point, PointJS}

import scala.scalajs.js

case class Triangle(
	pt1: Point,
	pt2: Point,
	pt3: Point
) {
	def points: Seq[Point] = Seq(pt1, pt2, pt3)
	def fromPoints(pts: Seq[Point]): Triangle = {
		assert(pts.size == 3)
		Triangle(pts.head, pts(1), pts(2))
	}

	def translate(vec: Point): Triangle = fromPoints(points.map(_.translate(vec)))

	def scaled(scaleFactor: Int): Triangle =
		Triangle(pt1.scaled(scaleFactor), pt2.scaled(scaleFactor), pt3.scaled(scaleFactor))

	// takes radians but produces a Triangle with Int points. Radians are small...
	def rotate(radians: Double): Triangle = {
		fromPoints(points.map(_.rotate(radians)))
	}

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
