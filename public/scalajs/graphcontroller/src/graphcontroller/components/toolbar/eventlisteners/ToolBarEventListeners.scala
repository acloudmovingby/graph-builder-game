package graphcontroller.components.toolbar.eventlisteners

import org.scalajs.dom
import org.scalajs.dom.html
import graphcontroller.controller.{Event, ToolBarMouseOut, ToolBarMouseOver, ToolSelected}
import graphcontroller.shared.EventListener

object ToolBarEventListeners extends EventListener {
  override def init(dispatch: Event => Unit): Unit = {
    val toolBar = dom.document.querySelector(".toolbar")
    val buttons = toolBar.querySelectorAll("button")
    buttons.foreach {
      case btn: html.Button =>
        btn.addEventListener("click", { (e: dom.MouseEvent) =>
          dispatch(ToolSelected(btn.id))
        })
        btn.addEventListener("mouseenter", { (e: dom.MouseEvent) =>
          dispatch(ToolBarMouseOver(btn.id))
        })
        btn.addEventListener("mouseleave", { (e: dom.MouseEvent) =>
          dispatch(ToolBarMouseOut)
        })
    }
  }
}

