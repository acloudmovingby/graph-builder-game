package graphcontroller.dataobject.canvas

import graphcontroller.dataobject.{Rectangle, Shape, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a rectangle with the HTML Canvas API */
case class RectangleCanvas(
	rect: Rectangle,
	style: ShapeStyle
) extends CanvasRenderOp, Shape {
	type This = RectangleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height)
		style.applyToPath(ctx)
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