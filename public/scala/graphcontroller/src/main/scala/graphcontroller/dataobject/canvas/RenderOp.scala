package graphcontroller.dataobject.canvas

// TODO just move CanvasLine and TriangleCanvas into this one file and make this a sealed trait?
trait RenderOp {
	def draw(ctx: org.scalajs.dom.CanvasRenderingContext2D): Unit
}

