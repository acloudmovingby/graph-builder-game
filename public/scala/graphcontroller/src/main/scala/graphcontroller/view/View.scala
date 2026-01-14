package graphcontroller.view

import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.NoSelection
import graphcontroller.view.adjacencymatrix.AdjacencyMatrixView

object View {
	def render(state: State): ViewData = {
		ViewData(
			AdjacencyMatrixView.render(state)
		)
	}
}