package graphcontroller.dataobject.canvas

import scala.scalajs.js
import js.JSConverters.*

/** Dataobject to hold a variety of shapes for drawing on the Canvas */
case class MultiShapesCanvas(
	lines: Seq[CanvasLine],
	triangles: Seq[TriangleCanvas]
) {
	def toJS: MultiShapesCanvasJS = js.Dynamic.literal(
		lines = this.lines.map(_.toJS).toJSArray,
		triangles = this.triangles.map(_.toJS).toJSArray
	).asInstanceOf[MultiShapesCanvasJS]

	def ++(m: MultiShapesCanvas): MultiShapesCanvas = MultiShapesCanvas(this.lines ++ m.lines, this.triangles ++ m.triangles)
}

/** JS compatible equivalent of MultiShapesCanvas */
@js.native
trait MultiShapesCanvasJS extends js.Object {
	val lines: js.Array[CanvasLineJS]
	val triangles: js.Array[TriangleCanvasJS]
}
