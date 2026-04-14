package graphcontroller.components.toolbar

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{EscapePressed, Event, ToolBarMouseOut, ToolBarMouseOver, ToolSelected}
import graphcontroller.model.State
import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, SelectTool, SelectMode}

object ToolBarComponent extends Component {
	override def update(state: State, event: Event): State = {
		event match {
			case ToolSelected(tool) =>
				val newTool = tool match {
					case "select" => SelectTool()
					case "basic" => BasicTool(None)
					case "area-complete" => AreaCompleteTool(false, Nil)
					case "magic-path" => MagicPathTool(None)
					case "move" => MoveTool(None)
					case _ => state.toolState
				}
				// Clear selection when switching away from SelectTool
				val newSelectedNodes = newTool match {
					case _: SelectTool => state.selectedNodes
					case _ => Set.empty[Int]
				}
				state.copy(toolState = newTool, selectedNodes = newSelectedNodes)
			case ToolBarMouseOver(tool) =>
				state.copy(hoveringOnTool = Some(tool))
			case ToolBarMouseOut =>
				state.copy(hoveringOnTool = None)
			case EscapePressed =>
				state.toolState match {
					case _: SelectTool if state.selectedNodes.nonEmpty =>
						// First press: clear selection, stay in SelectTool
						state.copy(selectedNodes = Set.empty)
					case _ =>
						// Not in SelectTool, or selection already empty: go to BasicTool
						state.copy(toolState = BasicTool(None), selectedNodes = Set.empty)
				}
			case _ => state
		}
	}

	override def view(state: State): RenderOp = ToolBarRenderData(
		selectedTool = state.toolState,
		hoveringOnTool = state.hoveringOnTool,
		state.featureFlags.selectTool
	)
}
