package graphcontroller.components

import graphcontroller.controller.Event
import graphcontroller.model.State

trait Component {
	/** Pure function that takes current state and input event and produces new state */
	def update(state: State, event: Event): State

	/**
	 * Side-effectful function that renders to dom, writes to clipboard, etc. Keep as minimal as possible
	 * or have sub-methods that are pure functions
	 * */
	def view(state: State): Unit
}