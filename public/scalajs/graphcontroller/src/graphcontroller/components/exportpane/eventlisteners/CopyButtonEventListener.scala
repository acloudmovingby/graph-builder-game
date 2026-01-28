package graphcontroller.components.exportpane.eventlisteners

import org.scalajs.dom

import graphcontroller.controller.{Event, CopyButtonClicked}
import graphcontroller.controller.eventlisteners.EventListener

object CopyButtonEventListener extends EventListener {
	override def init(dispatch: Event => Unit): Unit = {
		val btn = dom.document.getElementById("copy-btn")
		if (btn != null) {
			btn.addEventListener("click", (_: dom.Event) => dispatch(CopyButtonClicked))
		}
	}
}