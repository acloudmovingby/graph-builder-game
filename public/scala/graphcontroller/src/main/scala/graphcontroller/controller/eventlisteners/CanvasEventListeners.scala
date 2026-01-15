package graphcontroller.controller.eventlisteners

import org.scalajs.dom
import org.scalajs.dom.html

import graphcontroller.controller.{Controller, Event}

trait CanvasEventListeners {
    protected val elementId: String
    protected def canvasElement = dom.document.getElementById(elementId).asInstanceOf[html.Canvas]
    protected val scale = dom.window.devicePixelRatio

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

    def addEventListeners(): Unit = {
        def passEventToController(e: dom.Event)(f: dom.MouseEvent => Event): Unit = {
            Controller.handleEvent {
                f(e.asInstanceOf[dom.MouseEvent])
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
