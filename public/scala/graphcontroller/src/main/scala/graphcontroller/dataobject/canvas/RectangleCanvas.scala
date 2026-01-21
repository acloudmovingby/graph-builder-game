package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{Rectangle, Triangle, TriangleJS}
import org.scalajs.dom

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class RectangleCanvas(
	rect: Rectangle,
	color: String // Hex string, e.g. "#FF0000"
) extends RenderOp {
	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height)
		ctx.fillStyle = color
		ctx.fill()
	}
}