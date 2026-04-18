package graphcontroller.components.maincanvas.eventlisteners

import graphcontroller.controller.*
import graphcontroller.controller.MouseEvent.{DoubleClick, Down, Leave, Move, Up}
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.CanvasEventListeners
import org.scalajs.dom
import org.scalajs.dom.html

object MainCanvasEventListeners extends CanvasEventListeners {
	protected val elementId: String = "main-canvas-upper"

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

	def mouseDoubleClick(e: dom.MouseEvent): Event = {
		val coords = relativeCoordinates(e)
		MainCanvasMouseEvent(Vector2D(coords._1, coords._2), DoubleClick, shiftKey = e.shiftKey)
	}
}
