package graphcontroller.controller

import graphcontroller.components.Component
import graphcontroller.components.adjacencymatrix.AdjacencyMatrixComponent
import graphcontroller.components.exportpane.ExportPane
import graphcontroller.components.maincanvas.MainCanvasComponent
import graphcontroller.components.resizing.ResizingComponent
import graphcontroller.model.State

/**
 * (Theoretically) the ONE impure place in the code that mutates the application state (note the `var state`). This is
 * arguably the most important file in the entire codebase, as it is the central orchestrator that coordinates state updates and then
 * changing the view accordingly.
 */
object Controller {
	var isLive: Boolean = false // if is this a unit test or live application run
	var state: State = State.init

	private val components: Seq[Component] = Seq(
		AdjacencyMatrixComponent,
		ExportPane,
		MainCanvasComponent,
		ResizingComponent
	)

	def handleEvent(event: Event): Unit = {
		val newState = updateState(event, state)

		// Update the application state
		state = newState

		// Execute side effects to update the view
		val renderOps = components.map { c =>
			c.view(newState)
		}

		if (isLive) renderOps.foreach(_.render())
	}

	def updateState(event: Event, state: State): State = {
		components.foldLeft(state) { case (accumulatedState, c) =>
			c.update(accumulatedState, event)
		}
	}

}