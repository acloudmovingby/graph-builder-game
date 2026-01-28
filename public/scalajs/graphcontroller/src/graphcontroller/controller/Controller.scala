package graphcontroller.controller

import graphcontroller.components.Component
import graphcontroller.components.exportpane.ExportPane
import graphcontroller.model.{Model, State}
import graphcontroller.view.View

/**
 * (Theoretically) the ONE impure place in the code that mutates the application state. It then passes the view state
 * to the ViewUpdater which performs the side-effect of actually rendering the changes
 */
object Controller {
	// This can be private once we stop using old GraphController logic
	var state: State = State.init

	private val components: Seq[Component] = Seq(ExportPane)

	def handleEvent(event: Event): Unit = {
		/** Todo maybe just make Model a Component and rename it to be AdjMatrixComponent or something */
		val newState = Model.handleEvent(event, state)

		val newStateFromComponents = components.foldLeft(newState) { case (accumulatedState, c) =>
			c.update(accumulatedState, event)
		}

		// in the future, we can pass the old state if needed, or perhaps a new type that represents the diff ("StateChange" or something)
		// for now, just calculate all rendered stuff from scratch based on the new state
		// TODO: Consider naming "renderCommands" or "renderOps" instead of "newView"
		val newView = View.render(newStateFromComponents)

		// Execute side-effects to update the view
		ViewUpdater.updateView(newView)

		components.foreach { c =>
			c.view(newStateFromComponents)
		}

		// Update the application state
		state = newState
	}
}