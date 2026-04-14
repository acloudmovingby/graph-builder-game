package graphcontroller.components.toolbar.eventlisteners

import graphcontroller.controller.{DeleteSelectedNodes, EscapePressed, Event, ToolSelected}
import graphcontroller.shared.EventListener
import org.scalajs.dom

object KeyboardShortcutListeners extends EventListener {
  override def init(dispatch: Event => Unit): Unit = {
    dom.document.addEventListener("keydown", { (e: dom.KeyboardEvent) =>
      // Don't fire shortcuts when the user is typing in a text input
      e.target match {
        case elem: dom.html.Input    => ()
        case elem: dom.html.TextArea => ()
        case elem: dom.html.Select   => ()
        case _ =>
          // Only fire for bare key presses (no Cmd/Ctrl/Alt modifier)
          if (!e.metaKey && !e.ctrlKey && !e.altKey) {
            e.key match {
              case "v" | "V" => dispatch(ToolSelected("select"))
              case "b" | "B" => dispatch(ToolSelected("basic"))
              case "Escape"            => dispatch(EscapePressed)
              case "Delete" | "Backspace" => dispatch(DeleteSelectedNodes)
              case _                   => ()
            }
          }
      }
    })
  }
}
