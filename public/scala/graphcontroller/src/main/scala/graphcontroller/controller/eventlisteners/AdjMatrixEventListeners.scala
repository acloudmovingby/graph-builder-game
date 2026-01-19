package graphcontroller.controller.eventlisteners

import org.scalajs.dom
import org.scalajs.dom.html

import graphcontroller.controller.{
	AdjMatrixMouseDown, AdjMatrixMouseMove, AdjMatrixMouseUp, AdjMatrixMouseLeave, Controller, Event, NoOp
}

class AdjMatrixEventListeners extends CanvasEventListeners {
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

	def mouseLeave(e: dom.MouseEvent): Event = AdjMatrixMouseLeave
}
