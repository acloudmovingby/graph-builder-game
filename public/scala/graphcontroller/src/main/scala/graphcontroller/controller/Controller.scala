package graphcontroller.controller

import graphcontroller.model.{Model, State}

/**
 * The one place in the code that mutates the application state and renders the view.
 */
object Controller {
	// this can be private once we stop using old GraphController logic
	var state: State = State.init

	def handleEvent(event: Event): Unit = {
		val newState = Model.handleEvent(event, state)
		state = newState
	}
}