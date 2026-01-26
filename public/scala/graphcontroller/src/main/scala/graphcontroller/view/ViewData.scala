package graphcontroller.view

import graphcontroller.dataobject.canvas.RenderOp

case class ViewData(
	adjMatrix: AdjacencyMatrixViewData,
	mainCanvas: Seq[RenderOp]
)
