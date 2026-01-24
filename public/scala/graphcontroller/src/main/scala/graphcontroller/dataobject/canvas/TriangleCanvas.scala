package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{Shape, Triangle, TriangleJS, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a triangle with the HTML Canvas API */
case class TriangleCanvas(
	 tri: Triangle,
	 color: String // Hex string, e.g. "#FF0000"
) extends RenderOp, Shape {
	type This = TriangleCanvas

	def toJS: TriangleCanvasJS = js.Dynamic.literal(
		tri = this.tri.toJS,
		color = this.color
	).asInstanceOf[TriangleCanvasJS]

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.fillStyle = color
		ctx.beginPath()
		ctx.moveTo(tri.pt1.x, tri.pt1.y)
		ctx.lineTo(tri.pt2.x, tri.pt2.y)
		ctx.lineTo(tri.pt3.x, tri.pt3.y)
		ctx.closePath()
		ctx.fill()
	}

	def translate(vec: graphcontroller.dataobject.Vector2D): TriangleCanvas = this.copy(
		tri = tri.translate(vec)
	)

	def scaled(scaleFactor: Int): TriangleCanvas = this.copy(
		tri = tri.scaled(scaleFactor)
	)

	def rotate(radians: Double): TriangleCanvas = this.copy(
		tri = tri.rotate(radians)
	)
}

/** JS compatible equivalent of TriangleCanvas */
@js.native
trait TriangleCanvasJS extends js.Object {
	val tri: TriangleJS
	val color: String
}