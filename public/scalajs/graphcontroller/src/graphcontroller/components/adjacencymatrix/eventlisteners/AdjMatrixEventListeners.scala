package graphcontroller.components.adjacencymatrix.eventlisteners

import graphcontroller.components.CanvasEventListeners
import graphcontroller.controller.*
import org.scalajs.dom
import org.scalajs.dom.html

object AdjMatrixEventListeners extends CanvasEventListeners {
	protected val elementId: String = "adj-matrix"

	def mouseMove(e: dom.MouseEvent): Event = {
		getInBoundsCoordinates(e) match {
			case Some((x, y)) => AdjMatrixMouseMove(x, y)
			case None => NoOp
		}
	}

	def mouseDown(e: dom.MouseEvent): Event = {
		getInBoundsCoordinates(e) match {
			case Some((x, y)) => AdjMatrixMouseDown(x, y)
			case None => NoOp
		}
	}

	def mouseUp(e: dom.MouseEvent): Event = {
		getInBoundsCoordinates(e) match {
			case Some((x, y)) => AdjMatrixMouseUp(x, y)
			case None => NoOp
		}
	}

	def mouseLeave(e: dom.MouseEvent): Event = {
		val (x, y) = relativeCoordinates(e)
		AdjMatrixMouseLeave(x, y)
	}
}
