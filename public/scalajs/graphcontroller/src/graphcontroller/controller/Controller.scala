package graphcontroller.controller

import graphcontroller.components.Component
import graphcontroller.components.adjacencymatrix.AdjacencyMatrixComponent
import graphcontroller.components.exportpane.ExportPane
import graphcontroller.dataobject.AdjMatrixDimensions
import graphcontroller.model.State

/**
 * (Theoretically) the ONE impure place in the code that mutates the application state. It then passes the view state
 * to the ViewUpdater which performs the side-effect of actually rendering the changes
 */
object Controller {
	// This can be private once we stop using old GraphController logic
	var state: State = State.init

	private val components: Seq[Component] = Seq(ExportPane, AdjacencyMatrixComponent)

	def handleEvent(event: Event): Unit = {
		val newState = updateState(event, state)

		// Execute side effects to update the view
		components.foreach { c =>
			c.view(newState)
		}

		// Update the application state
		state = newState
	}

	// Uh, this is weird, but I'm trying to refactor some things and this makes the most sense
	def setAdjacencyMatrixParameters(event: Initialization): Unit = {
		state = state.copy(adjMatrixDimensions = AdjMatrixDimensions(event.adjMatrixWidth, event.adjMatrixHeight, event.padding, event.numberPadding))
	}

	def updateState(event: Event, state: State): State = {
		components.foldLeft(state) { case (accumulatedState, c) =>
			c.update(accumulatedState, event)
		}
	}

}