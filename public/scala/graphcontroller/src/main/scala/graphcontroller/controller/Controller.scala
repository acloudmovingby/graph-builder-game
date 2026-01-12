package graphcontroller.controller

import graphcontroller.model.{Model, State}
import graphcontroller.view.View

/**
 * The ONE place in the code that mutates the application state and renders the view. Well, probably not 100%, but
 * that's the idea.
 */
object Controller {
	// This can be private once we stop using old GraphController logic
	var state: State = State.init

	def handleEvent(event: Event): Unit = {
		val newState = Model.handleEvent(event, state)

		// in the future, we can pass the old state if needed, or perhaps a new type that represents the diff ("StateChange" or something)
		// for now, just calculate all rendered stuff from scratch based on the new state
		// TODO: Consider naming "renderCommands" or "renderOps" instead of "newView"
		val newView = View.render(newState)

		// Execute side-effects to update the view
		ViewUpdater.updateView(newView)

		// Update the application state
		state = newState
	}
}