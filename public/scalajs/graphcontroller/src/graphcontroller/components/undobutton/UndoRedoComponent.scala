package graphcontroller.components.undobutton

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.components.ops.{NoOp, RemoveAttribute, SetAttribute}
import graphcontroller.controller.{Event, RedoRequested, UndoRequested}
import graphcontroller.model.{HistoricalState, HoveredNode, State}
import graphcontroller.shared.{BasicTool, MagicPathTool}

object UndoRedoComponent extends Component {
	override def update(state: State, event: Event): State = {
		event match {
			case UndoRequested =>
				state.undoStack match {
					case prevState :: tail =>
						applyHistoryState(state, prevState).copy(
							undoStack = tail,
							redoStack = HistoricalState(state.graph, state.keyToData) :: state.redoStack
						)
					case Nil => state
				}
			case RedoRequested =>
				state.redoStack match {
					case nextState :: tail =>
						applyHistoryState(state, nextState).copy(
							undoStack = HistoricalState(state.graph, state.keyToData) :: state.undoStack,
							redoStack = tail
						)
					case Nil => state
				}
			case _ => state
		}
	}

	/** 
	 * Helper method to cleanup state when we undo/redo, since, among other things, node indices stored in State (e.g. 
	 * node hover) may no longer exist so we need to account for that 
	 * */
	private def applyHistoryState(state: State, historyState: HistoricalState[Int]): State = {
		// (1) Clear hovered node state if that node index no longer exists
		val hoveredNode = state.canvasInteraction.hoveredNode match {
			case Some(HoveredNode(nodeIndex, _)) if !historyState.graph.nodes.contains(nodeIndex) =>
				None
			case other => other
		}
		// (2) Clear tool state if in the middle of edge-adding mode, since it's weird to keep doing that during undo/redo
		val newToolState = state.toolState match {
			case BasicTool(Some(_)) => BasicTool(None)
			case MagicPathTool(Some(_)) => MagicPathTool(None)
			case _ => state.toolState
		}

		state
			.copy(
				graph = historyState.graph,
				keyToData = historyState.keyToData,
				toolState = newToolState
			)
			.setHoveredNode(hoveredNode)
	}

	override def view(state: State): RenderOp = {
		UndoRedoViewData(canUndo = state.undoStack.nonEmpty, canRedo = state.redoStack.nonEmpty)
	}
}

case class UndoRedoViewData(canUndo: Boolean, canRedo: Boolean) extends RenderOp {
	// TODO: Instead of the RenderOp containing two booleans and doing logic here, we could have it be RemoveAttribute, SetAttribute, etc.
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

		val redoBtn = org.scalajs.dom.document.getElementById("redo").asInstanceOf[org.scalajs.dom.html.Button]
		if (redoBtn != null) {
			if (canRedo) {
				redoBtn.removeAttribute("disabled")
				redoBtn.style.setProperty("background-image", "url('../images/redo-icon.svg')")
			} else {
				redoBtn.setAttribute("disabled", "true")
				redoBtn.style.setProperty("background-image", "url('../images/redo-icon-gray.svg')")
			}
		}
	}
}
