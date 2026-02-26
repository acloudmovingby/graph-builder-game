package graphcontroller.model

import graphcontroller.components.adjacencymatrix.{AdjMatrixInteractionState, NoSelection}
import graphcontroller.components.exportpane.ExportFormat
import graphi.{DirectedMapGraph, MapGraph, SimpleMapGraph}
import graphcontroller.dataobject.{AdjMatrixDimensions, Cell, Line, NodeData, Vector2D}
import graphcontroller.shared.{BasicTool, GraphRepresentation, Tool}

/** State of the whole program!! Well, not really, but like mostly... */
case class State(
	graph: MapGraph[Int],
	keyToData: Map[Int, NodeData],
	undoStack: List[GraphUndoState[Int]],
	adjMatrixState: AdjMatrixInteractionState,
	adjMatrixDimensions: AdjMatrixDimensions,
	copyToClipboard: Boolean = false,
	exportFormat: ExportFormat, // DOT, Python, etc.
	adjacencyExportType: GraphRepresentation, // whether exporting as list, matrix, etc. (for formats where that's applicable)
	toolState: Tool,
	hoveringOnNode: Option[HoveredNode], // whichever node the cursor is hovering over on the main canvas (if any),
	hoveringOnTool: Option[String],
	labelsVisible: Boolean = true,
	lastMainCanvasMousePosition: Vector2D = Vector2D(0, 0)
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

	/** Adds node with label as next highest index */
	def addNode(coords: Vector2D): State = {
		val nextIndex = graph.nodeCount
		this.pushUndoState.copy(graph = graph.addNode(nextIndex), keyToData = keyToData + (nextIndex -> NodeData(0, coords.x, coords.y)))
	}

	def addEdge(from: Int, to: Int): State = {
		this.pushUndoState.copy(graph = graph.addEdge(from, to))
	}

	/**
	 * Adds or removes multiple edges in a single operation, pushing only one undo state if changes occurred.
	 * TODO: Convert all the tuples to an Edge case class
	 */
	def bulkUpdateEdges(cells: Seq[(Int, Int)], isAdd: Boolean): State = {
		val newGraph = cells.foldLeft(this.graph) { (g, cell) =>
			if (cell._1 != cell._2) { // Currently we don't support self-loops
				if (isAdd) g.addEdge(cell._1, cell._2)
				else g.removeEdge(cell._1, cell._2)
			} else g
		}

		if (newGraph != this.graph) {
			this.pushUndoState.copy(graph = newGraph)
		} else {
			this
		}
	}

	/**
	 * Adds multiple nodes in a single operation, pushing only one undo state.
	 */
	def bulkAddNodes(coords: Seq[Vector2D]): State = {
		if (coords.isEmpty) this
		else {
			var currentGraph = this.graph
			var currentKeyToData = this.keyToData
			var nextIndex = currentGraph.nodeCount

			coords.foreach { coord =>
				currentGraph = currentGraph.addNode(nextIndex)
				currentKeyToData = currentKeyToData + (nextIndex -> NodeData(0, coord.x, coord.y))
				nextIndex += 1
			}

			this.pushUndoState.copy(graph = currentGraph, keyToData = currentKeyToData)
		}
	}

	def pushUndoState: State = {
		// The original idea to use a List for the undo stack was that it has efficient push/pop operations at the front,
		// but because of the stack's limited size, we end up traversing it (O(n)) to drop the oldest state when the limit
		// is reached, which will pretty much happen all the time once a user has been clicking around for a bit ... so
		// maybe a different data structure would be better
		val nodeCopyFunction = (i: Int) => i
		val newUndoState = GraphUndoState(graph.clone(nodeCopyFunction), keyToData)
		val newStack = (newUndoState :: undoStack).take(GraphUndoState.UNDO_SIZE_LIMIT)
		this.copy(undoStack = newStack)
	}

	def clearGraph(): State = {
		this.pushUndoState.copy(
			graph = graph.empty,
			keyToData = Map.empty,
			toolState = BasicTool(None),
			hoveringOnNode = None,
			hoveringOnTool = None
		)
	}

	def isDirected: Boolean = graph match {
		case _: DirectedMapGraph[Int] => true
		case _ => false
	}
}

object State {
	def init: State = State(
		graph = new DirectedMapGraph[Int](),
		keyToData = Map.empty,
		undoStack = List.empty,
		adjMatrixState = NoSelection,
		adjMatrixDimensions = AdjMatrixDimensions(100, 100, 10, 5), // override in Controller.init after loading settings
		exportFormat = ExportFormat.DOT,
		adjacencyExportType = GraphRepresentation.List,
		toolState = BasicTool(None),
		hoveringOnNode = None,
		hoveringOnTool = None
	)
}

case class HoveredNode(
	nodeIndex: Int,
	justAdded: Boolean // use this flag so that when we add a new node the hover effect doesn't immediately appear (not necessary but seems to look nicer)
)