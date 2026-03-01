package graphcontroller.components.cleargraphbutton

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{ClearButtonClicked, Event}
import graphcontroller.model.State

object ClearGraphComponent extends Component {
	override def update(state: State, event: Event): State = event match {
		case ClearButtonClicked => state.clearGraph()
		case _ => state
	}
}
