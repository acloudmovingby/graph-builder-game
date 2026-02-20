package graphcontroller.dataobject.canvas

import scala.scalajs.js
import graphcontroller.dataobject.{PointJS, Shape, Vector2D}
import org.scalajs.dom

import scala.scalajs.js.JSConverters.*

/** Represents data necessary to draw a line with the HTML Canvas API */
case class CanvasPolyLine(
	points: Seq[Vector2D],
	fillColor: Option[String],
	border: Option[Border]
) extends CanvasRenderOp, Shape {
	type This = CanvasPolyLine

	/*
			if (
		toolState.curTool === areaCompleteTool &&
		areaCompleteTool.state.mousePressed
	) {
		ctx.lineWidth = 1.5;
		ctx.strokeStyle = "red";
		ctx.fillStyle = "rgba(255, 130, 172, 0.15)";
		ctx.setLineDash([5, 5]);
		ctx.beginPath();
		let drawPoints = areaCompleteTool.state.drawPoints;
		let cur = drawPoints[0];
		for (let j = 1; j < drawPoints.length; j++) {
			cur = drawPoints[j];
			ctx.lineTo(cur.x, cur.y);
		}

		ctx.stroke();
		ctx.fill();
	}
	*/
	def draw(ctx: dom.CanvasRenderingContext2D): Unit = if (points.nonEmpty) {
		val originalLineWidth = ctx.lineWidth

		ctx.beginPath()

		ctx.moveTo(points.head.x, points.head.y) // safe because we check nonEmpty above
		// sliding window of two points, drawing line from one to the next
		points.tail.foreach { p =>
			ctx.lineTo(p.x, p.y)
		}

		// if we have a fill, do the fill
		fillColor.foreach { color =>
			ctx.fillStyle = color
			ctx.fill()
		}

		// if we have a border, do the stroke and then reset things
		border.foreach { b =>
			ctx.lineWidth = b.width
			ctx.strokeStyle = b.color
			ctx.setLineDash(b.lineDashSegments.map(_.toDouble).toJSArray)

			ctx.stroke();

			// reset things
			if (b.width != originalLineWidth) ctx.lineWidth = originalLineWidth
			// just always reset line dash to no line-dash
			if (b.lineDashSegments.nonEmpty) ctx.setLineDash(js.Array())
		}
	}

	def translate(vec: Vector2D): This = this.copy(
		points = this.points.map(p => p.translate(vec))
	)

	def scale(scaleFactor: Int): This = this.copy(
		points = this.points.map(p => p.scale(scaleFactor))
	)

	def rotate(radians: Double): This = this.copy(
		points = this.points.map(p => p.rotate(radians))
	)
}