package graphcontroller.components

import graphcontroller.controller.Event

trait EventListener {
	/** 
	 * Allows you to register/init the event listener with the controller. Every implementation of this trait
	 * will decide how to listen for input and how to make that into an Event object. It then will run the 'dispatch' callback
	 * to pass the Event to the Controller. 
	 * */
	def init(dispatch: Event => Unit): Unit
}