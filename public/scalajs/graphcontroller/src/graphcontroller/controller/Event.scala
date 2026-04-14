package graphcontroller.controller

import graphcontroller.components.exportpane.ExportFormat
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.GraphRepresentation

sealed trait Event

/**
 * Until I fully convert the tools.js code to ScalaJS, sometimes I just want to trigger an event so the Scala view code
 * re-renders based on some state change that came from tools.js code. So I pass down this NoOp event which doesn't change
 * the model but triggers a refresh of the view. */
case object NoOp extends Event

case object UndoRequested extends Event
case object RedoRequested extends Event

case object ClearButtonClicked extends Event
/** The button in the export pane is clicked, to copy the graph to the clipboard */
case object CopyButtonClicked extends Event
case class ExportFormatChanged(format: ExportFormat) extends Event
case class ExportAdjacencyTypeChanged(adjType: GraphRepresentation) extends Event

/** 
 * TODO: combine this somehow with the Adjacency matrix mouse events? Also instead of storing type as 
 * `eventType` in MainCanvasMouseEvent, maybe we just make into a subtype?
 * */
enum MouseEvent {
	// corresponds to JS `mousedown`, `mouseup`, etc. event handlers 
	case Move, Up, Down, Leave
}
case class MainCanvasMouseEvent(coords: Vector2D, eventType: MouseEvent, shiftKey: Boolean = false) extends Event

sealed trait AdjacencyMatrixEvent extends Event {
	val mouseX: Int
	val mouseY: Int
}

// Params to inject on page load
case class Initialization(
	adjMatrixWidth: Int,
	adjMatrixHeight: Int,
	padding: Int, // padding around the adjacency matrix
	numberPadding: Int // padding between the matrix and the row/column numbers
) extends Event

case class AdjMatrixMouseMove(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent

case class AdjMatrixMouseDown(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent

case class AdjMatrixMouseUp(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent

case class AdjMatrixMouseLeave(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent

case class ToolSelected(tool: String) extends Event
case class ToolBarMouseOver(tool: String) extends Event
case object ToolBarMouseOut extends Event
case object EscapePressed extends Event
case object DeleteSelectedNodes extends Event
case class CanvasDoubleClick(coords: Vector2D) extends Event
case object ToggleLabelsVisibility extends Event
case object ToggleDirectedness extends Event
case class HoverDirectednessIcon(isHover: Boolean) extends Event
