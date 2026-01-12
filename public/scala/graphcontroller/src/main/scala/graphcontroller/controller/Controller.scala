package graphcontroller.controller

import graphcontroller.model.State

object Controller {

	var state: State = State.init

	def handleEvent(event: Event): Unit = {
		event match {
			case AdjMatrixMouseMove(x, y) =>
				// Handle mouse move event
				//println(s"Mouse moved to position: ($x, $y)")
			// Add more event cases as needed
			case _ =>
				println("Unhandled event type")
		}
	}
}