package graphcontroller.components.undobutton.eventlisteners

import org.scalajs.dom
import graphcontroller.controller.{Event, UndoRequested}
import graphcontroller.shared.EventListener

object UndoEventListeners extends EventListener {
  override def init(dispatch: Event => Unit): Unit = {
    val undoBtn = dom.document.getElementById("undo")
    if (undoBtn != null) {
      undoBtn.addEventListener("click", { (e: dom.MouseEvent) =>
        dispatch(UndoRequested)
      })
    }

    dom.document.addEventListener("keydown", { (e: dom.KeyboardEvent) =>
      if (e.key.toLowerCase == "z" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault()
        dispatch(UndoRequested)
      }
    })
  }
}
