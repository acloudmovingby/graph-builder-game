package graphcontroller.controller

sealed trait Event

/**
 * Until I fully convert the tools.js code to ScalaJS, sometimes I just want to trigger an event so the Scala view code
 * re-renders based on some state change that came from tools.js code. So I pass down this NoOp event which doesn't change
 * the model but triggers a refresh of the view. */
case object NoOp extends Event

/** The button in the export pane is clicked, to copy the graph to the clipboard */
case object CopyButtonClicked extends Event

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
