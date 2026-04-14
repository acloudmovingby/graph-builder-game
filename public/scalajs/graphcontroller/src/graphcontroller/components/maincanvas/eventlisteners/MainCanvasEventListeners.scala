package graphcontroller.components.maincanvas.eventlisteners

import graphcontroller.controller.*
import graphcontroller.controller.MouseEvent.{Down, Leave, Move, Up}
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.CanvasEventListeners
import org.scalajs.dom
import org.scalajs.dom.html

object MainCanvasEventListeners extends CanvasEventListeners {
    protected val elementId: String = "main-canvas-upper"

    override def init(dispatch: Event => Unit): Unit = {
        super.init(dispatch)
        canvasElement.addEventListener("dblclick", { (e: dom.MouseEvent) =>
            val coords = relativeCoordinates(e)
            dispatch(CanvasDoubleClick(Vector2D(coords._1, coords._2)))
        })
    }

    // TODO this is obvious duplication here, if we can do similar to AdjacencyMatrix event listeners, centralize there
    //		maybe even make a common trait for mouse events that main canvas / adjacency matrix types inherit from
    def mouseMove(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseEvent(Vector2D(coords._1, coords._2), Move, shiftKey = e.shiftKey)
	}

    def mouseDown(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseEvent(Vector2D(coords._1, coords._2), Down, shiftKey = e.shiftKey)
	}

    def mouseUp(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseEvent(Vector2D(coords._1, coords._2), Up, shiftKey = e.shiftKey)
	}

    def mouseLeave(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseEvent(Vector2D(coords._1, coords._2), Leave, shiftKey = e.shiftKey)
	}
}
