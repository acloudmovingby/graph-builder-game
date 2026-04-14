package graphcontroller.dataobject.canvas

import graphcontroller.dataobject.{Shape, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a line with the HTML Canvas API */
case class CanvasLine(
	from: Vector2D,
	to: Vector2D,
	style: ShapeStyle
) extends CanvasRenderOp, Shape {
	type This = CanvasLine

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.moveTo(from.x, from.y)
		ctx.lineTo(to.x, to.y)
		style.applyToPath(ctx)
	}

	def translate(vec: Vector2D): CanvasLine = this.copy(
		from = from.translate(vec),
		to = to.translate(vec)
	)

	def scale(scaleFactor: Int): CanvasLine = this.copy(
		from = from.scale(scaleFactor),
		to = to.scale(scaleFactor)
	)

	def rotate(radians: Double): CanvasLine = this.copy(
		from = from.rotate(radians),
		to = to.rotate(radians)
	)
}