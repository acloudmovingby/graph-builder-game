package graphcontroller.components.buildpane

import graphcontroller.components.ops.{SetAttribute, SetInnerHTML}
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
		BuildPaneRenderOp(
			Seq(
				SetAttribute("visible-icon", "src", iconSrc),
				SetInnerHTML("node-count", state.graph.nodeCount.toString),
				SetInnerHTML("edge-count", state.graph.edgeCount.toString)
			)
		)
	}
}

case class BuildPaneRenderOp(ops: Seq[RenderOp]) extends RenderOp {
	override def render(): Unit = ops.foreach(_.render())
}
