package graphcontroller.view

import graphcontroller.components.adjacencymatrix.{AdjacencyMatrixView, NoSelection}
import graphcontroller.components.maincanvas.MainCanvasView
import graphcontroller.model.State

object View {
	def render(state: State): ViewData = {
		ViewData(
			MainCanvasView.render(state)
		)
	}
}