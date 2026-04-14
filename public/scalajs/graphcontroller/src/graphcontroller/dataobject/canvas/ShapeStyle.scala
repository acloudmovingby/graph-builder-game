package graphcontroller.dataobject.canvas

import scala.scalajs.js
import js.JSConverters.*
import org.scalajs.dom

/** Standardized styling for any canvas shape that draws a single path (fill + optional border).
 *  Shapes that use this should: (1) call ctx.beginPath(), (2) define their path, (3) call style.applyToPath(ctx).
 */
case class ShapeStyle(
	fillColor: Option[String] = None,
	border: Option[Border] = None
) {
	/** Apply fill and/or stroke to the current path on the canvas context, then reset any modified state. */
	def applyToPath(ctx: dom.CanvasRenderingContext2D): Unit = {
		fillColor.foreach { color =>
			ctx.fillStyle = color
			ctx.fill()
		}
		border.foreach { b =>
			ctx.strokeStyle = b.color
			ctx.lineWidth = b.width
			ctx.setLineDash(b.lineDashSegments.map(_.toDouble).toJSArray)
			ctx.stroke()
			if (b.lineDashSegments.nonEmpty) ctx.setLineDash(js.Array())
		}
	}
}

object ShapeStyle {
	def filled(color: String): ShapeStyle = ShapeStyle(fillColor = Some(color))

	def stroked(color: String, width: Double): ShapeStyle =
		ShapeStyle(border = Some(Border(color, width)))

	def filledAndStroked(fill: String, borderColor: String, borderWidth: Double): ShapeStyle =
		ShapeStyle(fillColor = Some(fill), border = Some(Border(borderColor, borderWidth)))
}
