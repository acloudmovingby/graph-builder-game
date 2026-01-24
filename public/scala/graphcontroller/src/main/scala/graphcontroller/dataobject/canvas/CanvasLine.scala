package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{PointJS, Shape, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a line with the HTML Canvas API */
case class CanvasLine(
	from: Vector2D,
	to: Vector2D,
	width: Int,
	color: String // Hex string, e.g. "#FF0000"
) extends RenderOp, Shape {
	type This = CanvasLine

	def toJS: CanvasLineJS = js.Dynamic.literal(
		from = this.from.toJS,
		to = this.to.toJS,
		width = this.width,
		color = this.color
	).asInstanceOf[CanvasLineJS]

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.moveTo(from.x, from.y)
		ctx.lineTo(to.x, to.y)
		ctx.lineWidth = width
		ctx.strokeStyle = color
		ctx.stroke()
	}

	def translate(vec: Vector2D): CanvasLine = this.copy(
		from = from.translate(vec),
		to = to.translate(vec)
	)

	def scaled(scaleFactor: Int): CanvasLine = this.copy(
		from = from.scaled(scaleFactor),
		to = to.scaled(scaleFactor)
	)

	def rotate(radians: Double): CanvasLine = this.copy(
		from = from.rotate(radians),
		to = to.rotate(radians)
	)
}

/** JS compatible equivalent of CanvasLine */
@js.native
trait CanvasLineJS extends js.Object {
	val from: PointJS
	val to: PointJS
	val width: Int
	val color: String
}