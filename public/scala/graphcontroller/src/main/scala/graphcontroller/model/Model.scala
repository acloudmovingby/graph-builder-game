package graphcontroller.model

import graphcontroller.controller.{AdjMatrixMouseDown, AdjacencyMatrixEvent, Event, Initialization, NoOp}
import graphcontroller.model.adjacencymatrix.{AdjMatrixClickDragLogic, ReleaseSelection}

/** Pure function that takes current state and the input event and then calculates the new state */
object Model {
	def handleEvent(event: Event, state: State): State = {
		val newState = event match {
			case e: Initialization => handleInitializationEvent(e, state)
			case e: AdjacencyMatrixEvent => handleAdjacencyMatrixEvent(e, state)
			case NoOp => state
		}
		newState
	}

	private def handleInitializationEvent(event: Initialization, state: State): State = {
		println("initializing model with adj matrix dimensions: " + event.adjMatrixWidth + "x" + event.adjMatrixHeight)
		state.copy(adjMatrixDimensions = (event.adjMatrixWidth, event.adjMatrixHeight))
	}

	private def handleAdjacencyMatrixEvent(event: AdjacencyMatrixEvent, state: State): State = {
		val newAdjMatrixState = AdjMatrixClickDragLogic.handleEvent(
			event,
			state.adjMatrixState,
			state.adjMatrixDimensions,
			state.graph.nodeCount,
			state.filledInCells
		)
		newAdjMatrixState match {
			case ReleaseSelection(cells, isAdd) =>
				val newGraph = cells.foldLeft(state.graph) { (graph, cell) =>
					if (cell.row != cell.col) { // Currently I don't allow self-loops
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