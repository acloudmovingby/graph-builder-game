package graphcontroller.controller

sealed trait Event

sealed trait AdjacencyMatrixEvent extends Event

// Params gathered at program startup
case class Initialization(adjMatrixWidth: Int, adjMatrixHeight: Int) extends Event

case class AdjMatrixMouseMove(x: Int, y: Int) extends AdjacencyMatrixEvent
case object AdjMatrixMouseDown extends AdjacencyMatrixEvent
case object AdjMatrixMouseLeave extends AdjacencyMatrixEvent
case object AdjMatrixMouseUp extends AdjacencyMatrixEvent
