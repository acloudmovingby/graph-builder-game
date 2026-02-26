package graphcontroller.components.cleargraphbutton.eventlisteners

import graphcontroller.controller.{ClearButtonClicked, Event}
import graphcontroller.shared.EventListener
import org.scalajs.dom

object ClearGraphButtonListener extends EventListener {
	override def init(dispatch: Event => Unit): Unit = {
		val btn = dom.document.getElementById("clear-btn")
		if (btn != null) {
			btn.addEventListener("click", (_: dom.Event) => dispatch(ClearButtonClicked))
		}
	}
}
