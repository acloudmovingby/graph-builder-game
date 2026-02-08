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
	def view(state: State): RenderOp
}

/** 
 * Contains the data (derived from State) that is needed to do the side-effectful rendering to the dom. Each component will
 * have its own collection of data and it will try to limit the side effectful logic to the render function as much as possible.
 * This will make the components more unit testable (not just their update functions, but also whatever they decide to draw).
 */
trait RenderOp {
	def render(): Unit
}

object RenderOp {
	val NoOp: RenderOp = () => ()
}