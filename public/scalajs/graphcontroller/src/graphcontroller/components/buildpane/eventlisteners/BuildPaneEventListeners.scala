package graphcontroller.components.buildpane.eventlisteners

import graphcontroller.controller.{Event, ToggleLabelsVisibility}
import graphcontroller.shared.EventListener
import org.scalajs.dom

object BuildPaneEventListeners extends EventListener {
  private val elementId: String = "label-visible-btn"

  override def init(dispatch: Event => Unit): Unit = {
    val element = dom.document.getElementById(elementId)
    if (element != null) {
      element.addEventListener("click", (e: dom.MouseEvent) => dispatch(ToggleLabelsVisibility))
    }
  }
}
