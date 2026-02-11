package graphcontroller.components.resizing

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, Initialization}
import graphcontroller.model.State

/**
 * The point of this Component is to just provide a place to handle resizing or other random changes to the dom that
 * don't fit into another more obvious component
 * */
object ResizingComponent extends Component {
	override def update(state: State, event: Event): State = event match {
		case Initialization(adjMatrixWidth, adjMatrixHeight, padding, numberPadding) =>
			val newState = state.copy(
				adjMatrixDimensions = state.adjMatrixDimensions.copy(
					canvasWidth = adjMatrixWidth,
					canvasHeight = adjMatrixHeight,
					padding = padding,
					numberPadding = numberPadding
				)
			)
			newState
		case _ => state
	}
}
