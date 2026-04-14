package graphcontroller.dataobject.canvas

import graphcontroller.dataobject.{Shape, Triangle, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class TriangleCanvas(
	 tri: Triangle,
	 style: ShapeStyle
) extends CanvasRenderOp, Shape {
	type This = TriangleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.moveTo(tri.pt1.x, tri.pt1.y)
		ctx.lineTo(tri.pt2.x, tri.pt2.y)
		ctx.lineTo(tri.pt3.x, tri.pt3.y)
		ctx.closePath()
		style.applyToPath(ctx)
	}

	def translate(vec: graphcontroller.dataobject.Vector2D): TriangleCanvas = this.copy(
		tri = tri.translate(vec)
	)

	def scale(scaleFactor: Int): TriangleCanvas = this.copy(
		tri = tri.scale(scaleFactor)
	)

	def rotate(radians: Double): TriangleCanvas = this.copy(
		tri = tri.rotate(radians)
	)
}