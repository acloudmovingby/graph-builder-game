package graphcontroller.model

import graphcontroller.components.exportpane.ExportFormat
import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.dataobject.{AdjMatrixDimensions, Cell, Line, NodeData, Vector2D}
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, NoSelection}
import graphcontroller.shared.GraphRepresentation

/** State of the whole program!! */
case class State(
	graph: DirectedMapGraph[Int] | SimpleMapGraph[Int],
	keyToData: Map[Int, NodeData],
	undoStack: List[GraphUndoState[Int]],
	adjMatrixState: AdjMatrixInteractionState,
	adjMatrixDimensions: AdjMatrixDimensions,
	copyToClipboard: Boolean = false,
	exportFormat: ExportFormat, // DOT, Python, etc.
	adjacencyExportType: GraphRepresentation // whether exporting as list, matrix, etc. (for formats where that's applicable)
) {
	/**
	 * Convenience method to get the filled-in cells in the adjacency matrix representation. Putting here with State because
	 * it's used by both Model and View code, but it depends on the graph stored in State.
	 *
	 * Note how we reverse row/col here.
	 * I found it more intuitive to have the matrix with rows as "from" and columns as "to"
	 * that way you can drag horizontally to add/remove edges from a single node to multiple nodes,
	 * or drag vertically to add/remove edges to a single node from multiple nodes.
	 */
	def filledInCells: Set[Cell] = graph.getEdges.map { (from, to) => Cell.fromEdge(from, to) }

	def getEdgeCoordinates(fromIndex: Int, toIndex: Int): Option[Line] = for {
		fromData <- keyToData.get(fromIndex)
		toData <- keyToData.get(toIndex)
	} yield Line(
		from = Vector2D(fromData.x, fromData.y),
		to = Vector2D(toData.x, toData.y)
	)
}

object State {
	def init: State = State(
		graph = new DirectedMapGraph[Int](),
		keyToData = Map.empty,
		// The original idea to use a List for the undo stack was that it has efficient push/pop operations at the front,
		// but because of the stack's limited size, we end up traversing it (O(n)) to drop the oldest state when the limit
		// is reached, which will pretty much happen all the time once a user has been clicking around for a bit ... so
		// maybe a different data structure would be better
		undoStack = List.empty,
		adjMatrixState = NoSelection,
		adjMatrixDimensions = AdjMatrixDimensions(100, 100, 10, 5), // override in Controller.init after loading settings
		exportFormat = ExportFormat.DOT,
		adjacencyExportType = GraphRepresentation.List
	)
}