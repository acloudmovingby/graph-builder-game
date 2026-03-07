package graphcontroller.components.undobutton.eventlisteners

import org.scalajs.dom
import graphcontroller.controller.{Event, RedoRequested, UndoRequested}
import graphcontroller.shared.EventListener

object UndoEventListeners extends EventListener {
  override def init(dispatch: Event => Unit): Unit = {
    val undoBtn = dom.document.getElementById("undo")
    if (undoBtn != null) {
      undoBtn.addEventListener("click", { (e: dom.MouseEvent) =>
        dispatch(UndoRequested)
      })
    }

    val redoBtn = dom.document.getElementById("redo")
    if (redoBtn != null) {
      redoBtn.addEventListener("click", { (e: dom.MouseEvent) =>
        dispatch(RedoRequested)
      })
    }

    dom.document.addEventListener("keydown", { (e: dom.KeyboardEvent) =>
      val isZ = e.key.toLowerCase == "z"
      val isY = e.key.toLowerCase == "y"
      val isCmdOrCtrl = e.metaKey || e.ctrlKey
      val isShift = e.shiftKey

      if (isCmdOrCtrl && isZ && isShift) {
        e.preventDefault()
        dispatch(RedoRequested)
      } else if (isCmdOrCtrl && isZ) {
        e.preventDefault()
        dispatch(UndoRequested)
      } else if (isCmdOrCtrl && isY) {
        e.preventDefault()
        dispatch(RedoRequested)
      }
    })
  }
}
