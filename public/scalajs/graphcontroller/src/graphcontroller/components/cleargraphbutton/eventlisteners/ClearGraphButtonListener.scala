package graphcontroller.components.cleargraphbutton.eventlisteners

import graphcontroller.controller.{ClearButtonClicked, Event}
import graphcontroller.shared.EventListener
import org.scalajs.dom

object ClearGraphButtonListener extends EventListener {

	/**
	 * Allows you to register/init the event listener with the controller. Every implementation of this trait
	 * will decide how to listen for input and how to make that into an Event object. It then will run the 'dispatch' callback
	 * to pass the Event to the Controller. 
	 * */
	override def init(dispatch: Event => Unit): Unit = {
		val btn = dom.document.getElementById("clear-btn")
		if (btn != null) {
			btn.addEventListener("click", (_: dom.Event) => dispatch(ClearButtonClicked))
		}
	}
}
