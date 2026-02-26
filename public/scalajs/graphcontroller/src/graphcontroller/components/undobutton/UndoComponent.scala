package graphcontroller.components.undobutton

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.components.ops.{NoOp, RemoveAttribute, SetAttribute}
import graphcontroller.controller.{Event, UndoRequested}
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{BasicTool, MagicPathTool}

object UndoComponent extends Component {
	override def update(state: State, event: Event): State = {
		event match {
			case UndoRequested =>
				state.undoStack.headOption match {
					case Some(prevState) =>
						// When we undo, node indices stored in State (e.g. node hover) may no longer exist so we need to account for that
						// (1) Clear hovered node state if that node index no longer exists
						val hoveredNode = state.hoveringOnNode match {
							case Some(HoveredNode(nodeIndex, _)) if !prevState.graph.nodes.contains(nodeIndex) =>
								None
							case other => other
						}
						// (2) Clear tool state if in the middle of edge-adding mode, since it's weird to keep doing that during undo
						val newToolState = state.toolState match {
							case BasicTool(Some(_)) => BasicTool(None)
							case MagicPathTool(Some(_)) => MagicPathTool(None)
							case _ => state.toolState
						}

						// Now return the new state with the undo applied
						state.copy(
							graph = prevState.graph,
							keyToData = prevState.keyToData,
							toolState = newToolState,
							hoveringOnNode = hoveredNode,
							undoStack = state.undoStack.tail
						)
					case None => state
				}
			case _ => state
		}
	}

	override def view(state: State): RenderOp = {
		UndoViewData(canUndo = state.undoStack.nonEmpty)
	}
}

case class UndoViewData(canUndo: Boolean) extends RenderOp {
	override def render(): Unit = {
		val undoBtn = org.scalajs.dom.document.getElementById("undo").asInstanceOf[org.scalajs.dom.html.Button]
		if (undoBtn != null) {
			if (canUndo) {
				undoBtn.removeAttribute("disabled")
				undoBtn.style.setProperty("background-image", "url('../images/undo-icon.svg')")
			} else {
				undoBtn.setAttribute("disabled", "true")
				undoBtn.style.setProperty("background-image", "url('../images/undo-icon-gray.svg')")
			}
		}
	}
}
