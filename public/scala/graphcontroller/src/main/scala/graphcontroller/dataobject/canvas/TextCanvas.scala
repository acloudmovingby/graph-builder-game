package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{PointJS, Shape, Vector2D}
import graphcontroller.dataobject.canvas.RenderOp
import org.scalajs.dom

/** Represents data necessary to draw a line with the HTML Canvas API */
case class TextCanvas(
	coords: Vector2D,
	text: String,
	color: String, // Hex string, e.g. "#FF0000"
	fontSize: Int
) extends RenderOp, Shape {
	type This = TextCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.fillStyle = color
		// center align the text vertically
		ctx.textBaseline = "middle"
		// center align the text horizontally
		ctx.textAlign = "center"
		ctx.fillText(text, coords.x.toDouble, coords.y.toDouble + 1) // +1 to better center vertically
	}

	def translate(vec: Vector2D): This = this.copy(
		coords = coords.translate(vec)
	)

	// Does not scale the font size, only the position of the text
	def scaled(scaleFactor: Int): This = this.copy(
		coords = coords.scaled(scaleFactor)
	)

	// Does not rotate the text, only rotate the coords position (relative to the origin I think?)
	def rotate(radians: Double): This = this.copy(
		coords = coords.rotate(radians)
	)
}