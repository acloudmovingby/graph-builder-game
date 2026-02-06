package graphcontroller.model

import graphcontroller.controller.{AdjMatrixMouseDown, AdjacencyMatrixEvent, Event, CopyButtonClicked, Initialization, NoOp}
import graphcontroller.dataobject.AdjMatrixDimensions
import graphcontroller.shared.AdjMatrixCoordinateConverter
import AdjMatrixCoordinateConverter.convertCoordinatesToZone
import graphcontroller.components.adjacencymatrix.{AdjMatrixInteractionLogic, ReleaseSelection}

/** Pure function that takes current state and the input event and then calculates the new state */
object Model {
	def handleEvent(event: Event, state: State): State = {
		val newState = event match {
			case e: Initialization => handleInitializationEvent(e, state)
			case _ => state
		}
		newState
	}

	private def handleInitializationEvent(event: Initialization, state: State): State = {
		state.copy(adjMatrixDimensions = AdjMatrixDimensions(event.adjMatrixWidth, event.adjMatrixHeight, event.padding, event.numberPadding))
	}
}