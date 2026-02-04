/*package graphcontroller.components.adjacencymatrix

import graphcontroller.components.Component
import graphcontroller.controller.{AdjacencyMatrixEvent, Event}
import graphcontroller.model.{Model, State}

object AdjacencyMatrix extends Component {
	/** Pure function that takes current state and input event and produces new state */
	override def update(state: State, event: Event): State = {
		event match {
			case e: AdjacencyMatrixEvent => Model.handleAdjacencyMatrixEvent(e, state)
			case _ => state
		}
	}

	override def view(state: State): Unit = {

	}
}

 */
