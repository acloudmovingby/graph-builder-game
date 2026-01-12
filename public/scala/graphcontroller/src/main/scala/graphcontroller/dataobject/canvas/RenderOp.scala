package graphcontroller.dataobject.canvas

import org.scalajs.dom.CanvasRenderingContext2D

// TODO just move CanvasLine and TriangleCanvas into this one file and make this a sealed trait?
trait RenderOp {
	def draw(ctx: CanvasRenderingContext2D): Unit
}

