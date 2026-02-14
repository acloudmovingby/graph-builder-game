package graphcontroller.components.maincanvas.eventlisteners

import graphcontroller.controller.*
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.CanvasEventListeners
import org.scalajs.dom
import org.scalajs.dom.html

object MainCanvasEventListeners extends CanvasEventListeners {
    protected val elementId: String = "main-canvas-upper"

    // TODO migrate existing tools.js logic to here and don't just do NoOp everywhere (but doing that here
    // so it actually triggers view update)
    def mouseMove(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseMove(Vector2D(coords._1, coords._2))
	}

    def mouseDown(e: dom.MouseEvent): Event = NoOp

    def mouseUp(e: dom.MouseEvent): Event = NoOp

    def mouseLeave(e: dom.MouseEvent): Event = NoOp
}
