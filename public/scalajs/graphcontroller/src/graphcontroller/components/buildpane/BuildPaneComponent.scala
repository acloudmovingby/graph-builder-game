package graphcontroller.components.buildpane

import graphcontroller.components.ops.SetAttribute
import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, ToggleLabelsVisibility}
import graphcontroller.model.State

object BuildPaneComponent extends Component {
  override def update(state: State, event: Event): State = {
    event match {
      case ToggleLabelsVisibility =>
        state.copy(labelsVisible = !state.labelsVisible)
      case _ => state
    }
  }

  override def view(state: State): RenderOp = {
    val iconSrc = if (state.labelsVisible) "images/node-label-visible.svg" else "images/invisible-icon.svg"
    SetAttribute("visible-icon", "src", iconSrc)
  }
}
