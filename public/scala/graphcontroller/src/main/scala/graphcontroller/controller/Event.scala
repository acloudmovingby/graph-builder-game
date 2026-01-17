package graphcontroller.controller

sealed trait Event

case object NoOp extends Event

sealed trait AdjacencyMatrixEvent extends Event

// Params gathered at program startup
case class Initialization(adjMatrixWidth: Int, adjMatrixHeight: Int) extends Event

case class AdjMatrixMouseMove(mouseX: Int, mouseY: Int) extends AdjacencyMatrixEvent
case object AdjMatrixMouseDown extends AdjacencyMatrixEvent
case object AdjMatrixMouseLeave extends AdjacencyMatrixEvent
case object AdjMatrixMouseUp extends AdjacencyMatrixEvent
