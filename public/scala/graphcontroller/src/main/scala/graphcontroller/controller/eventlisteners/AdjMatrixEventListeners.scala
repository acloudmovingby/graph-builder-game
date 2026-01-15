package graphcontroller.controller.eventlisteners

import org.scalajs.dom
import org.scalajs.dom.html

import graphcontroller.controller.{
    AdjMatrixMouseDown, AdjMatrixMouseMove, AdjMatrixMouseUp, AdjMatrixMouseLeave, Controller, Event, NoOp
}

class AdjMatrixEventListeners extends CanvasEventListeners {
    protected val elementId: String = "adj-matrix"

    def mouseMove(e: dom.MouseEvent): Event = {
        val (x, y) = relativeCoordinates(e)
        if (inBounds(x, y)) {
            AdjMatrixMouseMove(x, y)
        } else NoOp
    }

    def mouseDown(e: dom.MouseEvent): Event = AdjMatrixMouseDown

    def mouseUp(e: dom.MouseEvent): Event = AdjMatrixMouseUp

    def mouseLeave(e: dom.MouseEvent): Event = AdjMatrixMouseLeave
}
