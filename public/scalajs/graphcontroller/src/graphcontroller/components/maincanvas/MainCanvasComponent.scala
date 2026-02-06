package graphcontroller.components.maincanvas

import graphcontroller.components.Component
import graphcontroller.controller.Event
import graphcontroller.model.State
import graphcontroller.render.MainCanvas

object MainCanvasComponent extends Component {

	override def update(state: State, event: Event): State = {
		// currently main canvas event listeners don't do anything
		state
	}

	override def view(state: State): Unit = {
		val whatToRender = MainCanvasView.render(state)
		MainCanvas.setShapesNew(whatToRender)
	}
}
