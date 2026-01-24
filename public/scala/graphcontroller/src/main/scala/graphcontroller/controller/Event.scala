package graphcontroller.controller

sealed trait Event

case object NoOp extends Event

sealed trait AdjacencyMatrixEvent extends Event {
	val mouseX: Int
	val mouseY: Int
}

// Params gathered at program startup
case class Initialization(
	adjMatrixWidth: Int,
	adjMatrixHeight: Int,
	padding: Int,
	numberPadding: Int // padding between the matrix and the row/column numbers
) extends Event

case class AdjMatrixMouseMove(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent
case class AdjMatrixMouseDown(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent
case class AdjMatrixMouseUp(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent
case class AdjMatrixMouseLeave(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent
