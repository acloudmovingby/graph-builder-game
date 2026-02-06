package graphcontroller.view

import graphcontroller.components.adjacencymatrix.AdjacencyMatrixViewData
import graphcontroller.dataobject.canvas.RenderOp

case class ViewData(
	mainCanvas: Seq[RenderOp]
)
