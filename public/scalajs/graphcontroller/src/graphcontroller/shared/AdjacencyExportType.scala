package graphcontroller.shared

sealed trait AdjacencyExportType

/**
 * For several export formats (e.g. Python), this is the choice of whether to represent the graph
 * as an adjacency matrix, adjacency list, etc.
 * */
object AdjacencyExportType {
	case object List extends AdjacencyExportType
	case object Matrix extends AdjacencyExportType
}