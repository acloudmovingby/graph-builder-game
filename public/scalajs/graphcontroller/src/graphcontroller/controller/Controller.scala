package graphcontroller.controller

import graphcontroller.components.{Component, RenderOp}
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
	var state: State = State.init

	private val components: Seq[Component] = Seq(
		AdjacencyMatrixComponent,
		ExportPane,
		MainCanvasComponent,
		ResizingComponent
	)

	/** Side-effectful function that handles the event, mutates state, and re-renders the UI */
	def handleEvent(event: Event): Unit = {
		val (newState, renderOps) = handleEventWithState(event, state)

		// Update the application state
		state = newState

		// Execute side effects to update the view (i.e. change the dom)
		renderOps.foreach(_.render())
	}

	/** The highest level pure function we have, to be used in unit tests. */
	def handleEventWithState(event: Event, state: State): (State, Seq[RenderOp]) = {
		// Calculate new program state
		val newState = components.foldLeft(state) { case (accumulatedState, c) =>
			c.update(accumulatedState, event)
		}

		// derive the data needed to update the view from the new state
		val renderOps = components.map { c =>
			c.view(newState)
		}

		(newState, renderOps)
	}

}