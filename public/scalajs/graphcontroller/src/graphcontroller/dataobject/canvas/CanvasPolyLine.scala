package graphcontroller.dataobject.canvas

import graphcontroller.dataobject.{Shape, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a polyline with the HTML Canvas API */
case class CanvasPolyLine(
	points: Seq[Vector2D],
	style: ShapeStyle
) extends CanvasRenderOp, Shape {
	type This = CanvasPolyLine

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = if (points.nonEmpty) {
		ctx.beginPath()
		ctx.moveTo(points.head.x, points.head.y)
		points.tail.foreach { p =>
			ctx.lineTo(p.x, p.y)
		}
		style.applyToPath(ctx)
	}

	def translate(vec: Vector2D): This = this.copy(
		points = this.points.map(p => p.translate(vec))
	)

	def scale(scaleFactor: Int): This = this.copy(
		points = this.points.map(p => p.scale(scaleFactor))
	)

	def rotate(radians: Double): This = this.copy(
		points = this.points.map(p => p.rotate(radians))
	)
}