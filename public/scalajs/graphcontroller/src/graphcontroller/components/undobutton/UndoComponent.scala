package graphcontroller.components.undobutton

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.components.ops.{NoOp, RemoveAttribute, SetAttribute}
import graphcontroller.controller.{Event, UndoRequested}
import graphcontroller.model.State

object UndoComponent extends Component {
	override def update(state: State, event: Event): State = {
		event match {
			case UndoRequested =>
				state.undoStack.headOption match {
					case Some(prevState) =>
						state.copy(
							graph = prevState.graph,
							keyToData = prevState.keyToData,
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
