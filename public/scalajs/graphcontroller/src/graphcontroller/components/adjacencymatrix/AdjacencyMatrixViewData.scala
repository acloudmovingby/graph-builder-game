package graphcontroller.components.adjacencymatrix

import graphcontroller.dataobject.canvas.RenderOp

// TODO consider not having this case class if it only contains a sequence of RenderOps
// Or break it down into the components and add a render function on this to decide rendering order (THIS)
case class AdjacencyMatrixViewData(
	shapes: Seq[RenderOp]
)