package graphcontroller.view.maincanvas

import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.*
import graphcontroller.view.AdjacencyMatrixViewData
import graphi.MapGraph

object MainCanvasView {
	def render(state: State): Seq[RenderOp] = {
		Seq.empty
	}
}