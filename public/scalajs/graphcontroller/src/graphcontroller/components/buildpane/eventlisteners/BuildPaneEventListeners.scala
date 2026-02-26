package graphcontroller.components.buildpane.eventlisteners

import graphcontroller.controller.{Event, ToggleDirectedness, ToggleLabelsVisibility}
import graphcontroller.shared.EventListener
import org.scalajs.dom

object BuildPaneEventListeners extends EventListener {

	override def init(dispatch: Event => Unit): Unit = {
		// TODO: De-dupe / abstract away commonality here? Maybe a generic 'toggle button listener'?
		val nodeLabelToggle = dom.document.getElementById("label-visible-btn")
		if (nodeLabelToggle != null) {
			nodeLabelToggle.addEventListener("click", (e: dom.MouseEvent) => dispatch(ToggleLabelsVisibility))
		}
		
		val directednessToggle = dom.document.getElementById("directed-btn")
		if (directednessToggle != null) {
			directednessToggle.addEventListener("click", (e: dom.MouseEvent) => dispatch(ToggleDirectedness))
		}
	}
}
