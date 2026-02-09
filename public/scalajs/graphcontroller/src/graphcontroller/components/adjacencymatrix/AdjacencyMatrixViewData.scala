package graphcontroller.components.adjacencymatrix

import graphcontroller.components.RenderOp
import graphcontroller.dataobject.canvas.CanvasRenderOp

// TODO consider not having this case class if it only contains a sequence of RenderOps
// Or break it down into the components and add a render function on this to decide rendering order (THIS)
case class AdjacencyMatrixViewData(
	shapes: Seq[CanvasRenderOp]
) extends RenderOp {
	def render(): Unit = {
		AdjMatrixCanvas.setShapes(shapes)
	}
}