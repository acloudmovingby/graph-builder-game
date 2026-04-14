package graphcontroller.dataobject.canvas

import scala.scalajs.js
import js.JSConverters.*
import graphcontroller.dataobject.{Rectangle, Shape, Triangle, TriangleJS, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a rectangle with the HTML Canvas API */
case class RectangleCanvas(
	rect: Rectangle,
	fillColor: String, // Hex string or rgba(), e.g. "#FF0000" or "rgba(0,0,0,0)"
	borderColor: Option[String] = None,
	borderWidth: Option[Double] = None,
	lineDashSegments: Seq[Int] = Seq.empty
) extends CanvasRenderOp, Shape {
	type This = RectangleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		if (lineDashSegments.nonEmpty) {
			ctx.setLineDash(lineDashSegments.map(_.toDouble).toJSArray)
		}
		ctx.beginPath()
		ctx.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height)
		ctx.fillStyle = fillColor
		ctx.fill()
		borderColor.foreach { color =>
			ctx.strokeStyle = color
			ctx.lineWidth = borderWidth.getOrElse(1.0)
			ctx.stroke()
		}
		if (lineDashSegments.nonEmpty) {
			ctx.setLineDash(js.Array()) // reset dash after drawing
		}
	}

	def translate(vec: Vector2D): RectangleCanvas = this.copy(
		rect = rect.translate(vec)
	)

	def scale(scaleFactor: Int): RectangleCanvas = this.copy(
		rect = rect.scale(scaleFactor)
	)

	def rotate(radians: Double): RectangleCanvas = this.copy(
		rect = rect.rotate(radians)
	)
}