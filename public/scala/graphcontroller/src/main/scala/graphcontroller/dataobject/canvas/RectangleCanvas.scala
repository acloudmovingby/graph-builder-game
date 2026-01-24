package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{Rectangle, Shape, Triangle, TriangleJS, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class RectangleCanvas(
	rect: Rectangle,
	color: String // Hex string, e.g. "#FF0000"
) extends RenderOp, Shape {
	type This = RectangleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height)
		ctx.fillStyle = color
		ctx.fill()
	}

	def translate(vec: Vector2D): RectangleCanvas = this.copy(
		rect = rect.translate(vec)
	)

	def scaled(scaleFactor: Int): RectangleCanvas = this.copy(
		rect = rect.scaled(scaleFactor)
	)

	def rotate(radians: Double): RectangleCanvas = this.copy(
		rect = rect.rotate(radians)
	)
}