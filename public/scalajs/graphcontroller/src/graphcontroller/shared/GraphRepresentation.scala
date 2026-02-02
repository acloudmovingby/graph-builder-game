package graphcontroller.shared

sealed trait GraphRepresentation

/**
 * For several export formats (e.g. Python), this is the choice of whether to represent the graph
 * as an adjacency matrix, adjacency list, etc.
 * */
object GraphRepresentation {
	case object List extends GraphRepresentation
	case object Matrix extends GraphRepresentation
}