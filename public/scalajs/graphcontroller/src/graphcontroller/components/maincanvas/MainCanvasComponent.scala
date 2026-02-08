package graphcontroller.components.maincanvas

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.Event
import graphcontroller.model.State

object MainCanvasComponent extends Component {

	override def update(state: State, event: Event): State = {
		// currently main canvas event listeners don't do anything
		state
	}

	override def view(state: State): RenderOp = MainCanvasView.render(state)
}
