package graphcontroller.components

import graphcontroller.controller.{Controller, Event}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas

trait CanvasEventListeners extends EventListener {
	protected val elementId: String

	protected def canvasElement: Canvas = dom.document.getElementById(elementId).asInstanceOf[html.Canvas]

	protected def scale = dom.window.devicePixelRatio

	// Calculate the x y coordinates relative to the bounding box of the canvas element
	protected def relativeCoordinates(e: dom.MouseEvent): (Int, Int) = {
		val rect = canvasElement.getBoundingClientRect()
		val x = (e.clientX - rect.left).toInt
		val y = (e.clientY - rect.top).toInt
		(x, y)
	}

	protected def canvasWidth: Int = (canvasElement.width / scale).toInt

	protected def canvasHeight: Int = (canvasElement.height / scale).toInt

	protected def inBounds(x: Int, y: Int): Boolean = {
		x >= 0 && x <= canvasWidth && y >= 0 && y <= canvasHeight
	}

	protected def getInBoundsCoordinates(e: dom.MouseEvent): Option[(Int, Int)] = {
		val (mouseX, mouseY) = relativeCoordinates(e)
		if (inBounds(mouseX, mouseY)) Some((mouseX, mouseY)) else None
	}

	override def init(dispatch: Event => Unit): Unit = {
		// helper to reduce code duplication, but, uh, like lots of abstractions, turns out is pretty confusing
		// Take the dom event (dom.Event) and turn it into our internal Event type, then pass that to the Controller via dispatch
		// By making Events, we can then isolate our app logic from the DOM API and make it more testable
		def passEventToController(e: dom.Event)(f: dom.MouseEvent => Event): Unit = {
			dispatch {
				f(e.asInstanceOf[dom.MouseEvent]) // not sure why I need to cast here
			}
		}

		canvasElement.addEventListener("mousemove", (e: dom.Event) => passEventToController(e)(mouseMove))
		canvasElement.addEventListener("mouseleave", (e: dom.Event) => passEventToController(e)(mouseLeave))
		canvasElement.addEventListener("mouseup", (e: dom.Event) => passEventToController(e)(mouseUp))
		canvasElement.addEventListener("mousedown", (e: dom.Event) => passEventToController(e)(mouseDown))
	}

	def mouseMove(e: dom.MouseEvent): Event

	def mouseDown(e: dom.MouseEvent): Event

	def mouseUp(e: dom.MouseEvent): Event

	def mouseLeave(e: dom.MouseEvent): Event
}
