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
	undoStack: List[HistoricalState[Int]],
	redoStack: List[HistoricalState[Int]],
	adjMatrixState: AdjMatrixInteractionState,
	adjMatrixDimensions: AdjMatrixDimensions,
	copyToClipboard: Boolean = false,
	exportFormat: ExportFormat, // DOT, Python, etc.
	adjacencyExportType: GraphRepresentation, // whether exporting as list, matrix, etc. (for formats where that's applicable)
	toolState: Tool,
	hoveringOnTool: Option[String],
	labelsVisible: Boolean = true,
	hoverDirectedIcon: Boolean = false,
	canvasInteraction: MainCanvasInteractionState,
	featureFlags: FeatureFlags,
	selectedNodes: Set[Int] = Set.empty // not part of HistoricalState; selection is not undoable
) {
	/** Memoized */
	lazy val sortedNodes = this.graph.nodes.sorted
	private lazy val indicesToNodes: Map[Int, Int] = sortedNodes.zipWithIndex.map((node, ix) => (ix, node)).toMap

	/**
	 * For things like the adjacency matrix we want to know the 0-based index of the node (every node will be in a continuous
	 * range of 0 up to nodeCount). We want this because initially nodes are added with contiguous numbers but later the user
	 * can delete nodes which means their int label no longer aligns with their index */
	def nodeIndex(node: Int): Int = sortedNodes.indexOf(node)

	/**
	 * Convenience method to get the filled-in cells in the adjacency matrix representation. Putting here with State because
	 * it's used by both Model and View code, but it depends on the graph stored in State.
	 *
	 * Note how we reverse row/col here.
	 * I found it more intuitive to have the matrix with rows as "from" and columns as "to"
	 * that way you can drag horizontally to add/remove edges from a single node to multiple nodes,
	 * or drag vertically to add/remove edges to a single node from multiple nodes.
	 */
	def filledInCells: Seq[Cell] = graph.getEdges.map { (from, to) =>
		Cell.fromEdge(nodeIndex(from), nodeIndex(to))
	}
	
	def cellToNodeTuple(c: Cell): (Int, Int) = (indicesToNodes(c.row), indicesToNodes(c.col)) 

	def getEdgeCoordinates(fromIndex: Int, toIndex: Int): Option[Line] = for {
		fromData <- keyToData.get(fromIndex)
		toData <- keyToData.get(toIndex)
	} yield Line(
		from = Vector2D(fromData.x, fromData.y),
		to = Vector2D(toData.x, toData.y)
	)

	/**
	 * Adds node with label as next highest index. For convenience (this is used many times in unit tests) it assumes you
	 * want to store this change on the undo stack and defaults that to true.
	 * */
	def addNode(coords: Vector2D, pushUndoState: Boolean = true): State = {
		// So I'm making it so that if you delete a node and then add a new one, the new one will have an index one higher
		// than the current max. You could do it by filling in the missing indices but I find that strange? You could also
		// make an absolutely incrementing number so it's always higher than any other node ever created but I find that weird too
		// I dunno...there's not a single good solution
		val nextNodeLabel = graph.nodes.maxOption.getOrElse(-1) + 1
		val state = if (pushUndoState) this.pushUndoState else this
		state.copy(
			graph = graph.addNode(nextNodeLabel),
			keyToData = keyToData + (nextNodeLabel -> NodeData(0, coords.x, coords.y))
		)
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
		if (coords.isEmpty) this // if empty, don't add to undo state, just return
		else {
			// add to undo state _once_ and then call addNode repeatedly
			coords.foldLeft(this.pushUndoState) { case (state, coord) =>
				state.addNode(coord, pushUndoState = false) // don't add undo state every time
			}
		}
	}

	def removeNode(node: Int, pushUndoState: Boolean = true): State = {
		val newHoveredNode = this.canvasInteraction.hoveredNode match {
			case Some(node) => None
			case other => other
		}
		val state = if (pushUndoState) this.pushUndoState else this
		state.copy(
			graph = state.graph.removeNode(node),
			keyToData = state.keyToData.removed(node),
			selectedNodes = state.selectedNodes.filterNot(_ == node),
			canvasInteraction = state.canvasInteraction.copy(hoveredNode = newHoveredNode),
		)
	}

	def bulkRemoveNodes(nodes: Seq[Int]): State = {
		if (nodes.isEmpty) this // if empty, don't add to undo state, just return
		else {
			// add to undo state _once_ and then call removeNode repeatedly
			nodes.foldLeft(this.pushUndoState) { case (state, node) =>
				state.removeNode(node, pushUndoState = false) // don't add undo state every time
			}
		}
	}

	def pushUndoState: State = {
		// The original idea to use a List for the undo stack was that it has efficient push/pop operations at the front,
		// but because of the stack's limited size, we end up traversing it (O(n)) to drop the oldest state when the limit
		// is reached, which will pretty much happen all the time once a user has been clicking around for a bit ... so
		// maybe a different data structure would be better
		val newUndoState = HistoricalState(graph, keyToData)
		val newStack = (newUndoState :: undoStack).take(HistoricalState.UNDO_SIZE_LIMIT)
		this.copy(undoStack = newStack, redoStack = List.empty)
	}

	def clearGraph(): State = {
		this.pushUndoState.copy(
			graph = graph.empty,
			keyToData = Map.empty,
			toolState = BasicTool(None),
			canvasInteraction = this.canvasInteraction.copy(None, this.canvasInteraction.lastMousePosition),
			hoveringOnTool = None
		)
	}

	def isDirected: Boolean = graph match {
		case _: DirectedMapGraph[Int] => true
		case _: SimpleMapGraph[Int] => false
	}

	def setHoveredNode(h: Option[HoveredNode]): State = this.copy(
		canvasInteraction = this.canvasInteraction.copy(hoveredNode = h)
	)

	/** Find all nodes whose centres fall within the rectangle defined by two corners (order-independent). */
	def nodesInRect(corner1: Vector2D, corner2: Vector2D): Set[Int] = {
		val minX = math.min(corner1.x, corner2.x)
		val maxX = math.max(corner1.x, corner2.x)
		val minY = math.min(corner1.y, corner2.y)
		val maxY = math.max(corner1.y, corner2.y)
		keyToData.collect {
			case (key, data) if data.x >= minX && data.x <= maxX && data.y >= minY && data.y <= maxY => key
		}.toSet
	}
}

object State {
	def init: State = State(
		graph = new DirectedMapGraph[Int](),
		keyToData = Map.empty,
		undoStack = List.empty,
		redoStack = List.empty,
		adjMatrixState = NoSelection,
		adjMatrixDimensions = AdjMatrixDimensions(100, 100, 10, 5), // override in Controller.init after loading settings
		exportFormat = ExportFormat.DOT,
		adjacencyExportType = GraphRepresentation.List,
		toolState = BasicTool(None),
		hoveringOnTool = None,
		canvasInteraction = MainCanvasInteractionState(hoveredNode = None, lastMousePosition = Vector2D(0, 0)),
		featureFlags = FeatureFlags(selectTool = true)
	)
}

case class HoveredNode(
	nodeIndex: Int,
	justAdded: Boolean // use this flag so that when we add a new node the hover effect doesn't immediately appear (not necessary but seems to look nicer)
)

case class FeatureFlags(
	selectTool: Boolean
)

case class MainCanvasInteractionState(
	hoveredNode: Option[HoveredNode], // whichever node the cursor is hovering over on the main canvas (if any),
	lastMousePosition: Vector2D
)