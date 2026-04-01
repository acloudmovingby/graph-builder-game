package graphcontroller.components.toolbar

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, ToolBarMouseOut, ToolBarMouseOver, ToolSelected}
import graphcontroller.model.State
import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, SelectTool}

object ToolBarComponent extends Component {
	override def update(state: State, event: Event): State = {
		event match {
			case ToolSelected(tool) =>
				val newTool = tool match {
					case "select" => SelectTool(mousePressedStartPoint = None)
					case "basic" => BasicTool(None)
					case "area-complete" => AreaCompleteTool(false, Nil)
					case "magic-path" => MagicPathTool(None)
					case "move" => MoveTool(None)
					case _ => state.toolState
				}
				state.copy(toolState = newTool)
			case ToolBarMouseOver(tool) =>
				state.copy(hoveringOnTool = Some(tool))
			case ToolBarMouseOut =>
				state.copy(hoveringOnTool = None)
			case _ => state
		}
	}

	override def view(state: State): RenderOp = ToolBarRenderData(
		selectedTool = state.toolState,
		hoveringOnTool = state.hoveringOnTool,
		state.featureFlags.selectTool
	)
}
