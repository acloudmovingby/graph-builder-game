package graphcontroller.dataobject.canvas

import scala.scalajs.js
import js.JSConverters.*
import graphcontroller.dataobject.{Shape, Circle, Vector2D}
import org.scalajs.dom

/** Represents data necessary to draw a Circle with the HTML Canvas API */
case class CircleCanvas(
	circ: Circle,
	fillColor: Option[String], // Hex string, e.g. "#FF0000"
	borderColor: Option[String], // Hex string, e.g. "#FF0000"
	borderWidth: Option[Double],
	lineDashSegments: Seq[Int] = Seq.empty // TODO: combine these last 3 into one Border case class rather than all 3 as Option
) extends CanvasRenderOp, Shape {
	type This = CircleCanvas

	def draw(ctx: dom.CanvasRenderingContext2D): Unit = {
		ctx.beginPath()
		ctx.arc(circ.center.x, circ.center.y, circ.radius, 0, 2 * Math.PI)

		fillColor.foreach { color =>
			ctx.fillStyle = color
			ctx.arc(circ.center.x, circ.center.y, circ.radius, 0, 2 * Math.PI)
			ctx.fill()
		}

		borderColor.foreach { color =>
			ctx.strokeStyle = color
			ctx.lineWidth = borderWidth.getOrElse(1.0)
			if (lineDashSegments.nonEmpty) {
				ctx.setLineDash(lineDashSegments.map(_.toDouble).toJSArray)
			}
			ctx.stroke()
		}
		
		if (lineDashSegments.nonEmpty) ctx.setLineDash(js.Array.apply())
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