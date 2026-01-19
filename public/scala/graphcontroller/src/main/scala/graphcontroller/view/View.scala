package graphcontroller.view

import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.NoSelection
import graphcontroller.view.adjacencymatrix.AdjacencyMatrixView
import graphcontroller.view.maincanvas.MainCanvasView

object View {
	def render(state: State): ViewData = {
		ViewData(
			AdjacencyMatrixView.render(state),
			MainCanvasView.render(state)
		)
	}
}