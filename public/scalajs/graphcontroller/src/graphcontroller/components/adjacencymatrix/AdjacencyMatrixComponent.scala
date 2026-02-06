package graphcontroller.components.adjacencymatrix

import graphcontroller.components.Component
import graphcontroller.controller.{AdjacencyMatrixEvent, Event}
import graphcontroller.model.{Model, State}
import graphcontroller.render.AdjMatrixCanvas
import graphcontroller.shared.AdjMatrixCoordinateConverter.convertCoordinatesToZone

object AdjacencyMatrixComponent extends Component {
	/** Pure function that takes current state and input event and produces new state */
	override def update(state: State, event: Event): State = {
		event match {
			case e: AdjacencyMatrixEvent => handleAdjacencyMatrixEvent(e, state)
			case _ => state
		}
	}

	override def view(state: State): Unit = {
		val viewData = AdjacencyMatrixView.render(state)
		AdjMatrixCanvas.setShapes(viewData.shapes)
	}

	private def handleAdjacencyMatrixEvent(event: AdjacencyMatrixEvent, state: State): State = {
		val zone = convertCoordinatesToZone(
			event.mouseX,
			event.mouseY,
			state.adjMatrixDimensions,
			state.graph.nodeCount
		)

		// calculate change in adjacency matrix state
		val newAdjMatrixState = AdjMatrixInteractionLogic.handleMouseEvent(
			event,
			state.adjMatrixState,
			state.graph.nodeCount,
			zone,
			state.filledInCells
		)

		newAdjMatrixState match {
			// if the state changed to a ReleaseSelection, the selection was released and therefore we need to update the graph accordingly
			case ReleaseSelection(cells, isAdd) =>
				val newGraph = cells.foldLeft(state.graph) { (graph, cell) =>
					if (cell.row != cell.col) { // Currently we don't support self-loops
						if (isAdd) {
							graph.addEdge(cell.row, cell.col)
						} else {
							graph.removeEdge(cell.row, cell.col)
						}
					} else graph
				}

				state.copy(
					graph = newGraph,
					adjMatrixState = newAdjMatrixState
				)
			case _ => state.copy(adjMatrixState = newAdjMatrixState)
		}
	}
}
