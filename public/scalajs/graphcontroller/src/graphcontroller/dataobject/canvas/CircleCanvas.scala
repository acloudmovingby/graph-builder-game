package graphcontroller.dataobject.canvas

import graphcontroller.dataobject.{Shape, Circle, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a Circle with the HTML Canvas API */
case class CircleCanvas(
	circ: Circle,
	style: ShapeStyle
) extends CanvasRenderOp, Shape {
	type This = CircleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.arc(circ.center.x, circ.center.y, circ.radius, 0, 2 * Math.PI)
		style.applyToPath(ctx)
	}

	def translate(vec: graphcontroller.dataobject.Vector2D): CircleCanvas = this.copy(
		circ = circ.translate(vec)
	)

	def scale(scaleFactor: Int): CircleCanvas = this.copy(
		circ = circ.scale(scaleFactor)
	)

	def rotate(radians: Double): CircleCanvas = this.copy(
		circ = circ.rotate(radians)
	)
}