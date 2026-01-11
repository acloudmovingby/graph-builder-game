package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{Triangle, TriangleJS}
import org.scalajs.dom

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class RectangleCanvas(
	 x: Int,
	 y: Int,
	 width: Int,
	 height: Int,
	 color: String // Hex string, e.g. "#FF0000"
) extends RenderOp {
	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.rect(x, y, width, height)
		ctx.fillStyle = color
		ctx.fill()
	}
}