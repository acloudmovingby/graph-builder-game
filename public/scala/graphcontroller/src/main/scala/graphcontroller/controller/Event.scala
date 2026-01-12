package graphcontroller.controller

sealed trait Event

sealed trait AdjacencyMatrixEvent extends Event

case class AdjMatrixMouseMove(x: Int, y: Int) extends AdjacencyMatrixEvent
case object AdjMatrixMouseDown extends AdjacencyMatrixEvent
case object AdjMatrixMouseLeave extends AdjacencyMatrixEvent
case object AdjMatrixMouseUp extends AdjacencyMatrixEvent