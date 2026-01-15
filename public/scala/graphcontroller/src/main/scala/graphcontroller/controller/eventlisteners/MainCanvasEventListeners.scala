package graphcontroller.controller.eventlisteners

import graphcontroller.controller.*
import org.scalajs.dom
import org.scalajs.dom.html

class MainCanvasEventListeners extends CanvasEventListeners {
    protected val elementId: String = "canvas"

    // TODO migrate existing tools.js logic to here and don't just do NoOp everywhere (but doing that here
    // so it actually triggers view update)
    def mouseMove(e: dom.MouseEvent): Event = NoOp

    def mouseDown(e: dom.MouseEvent): Event = NoOp

    def mouseUp(e: dom.MouseEvent): Event = NoOp

    def mouseLeave(e: dom.MouseEvent): Event = NoOp
}
