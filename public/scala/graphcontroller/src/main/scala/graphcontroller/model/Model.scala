package graphcontroller.model

import graphcontroller.controller.{AdjMatrixMouseDown, AdjacencyMatrixEvent, Event}
import graphcontroller.model.adjacencymatrix.AdjMatrixClickDragLogic

/** Pure function that takes current state and the input event and then calculates the new state */
object Model {
	def handleEvent(event: Event, state: State): State = {
		val newState = event match {
			case e: AdjacencyMatrixEvent => handleAdjacencyMatrixEvent(e, state)
		}
		newState
	}

	private def handleAdjacencyMatrixEvent(event: AdjacencyMatrixEvent, state: State): State = {
		val newAdjMatrixState = AdjMatrixClickDragLogic.handleEvent(event, state.adjMatrixState)
		state.copy(adjMatrixState = newAdjMatrixState)
	}
}
