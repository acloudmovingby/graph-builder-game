package graphcontroller.model

import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.dataobject.NodeData
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, NoSelection}

case class State(
	graph: DirectedMapGraph[Int] | SimpleMapGraph[Int],
	keyToData: Map[Int, NodeData],
	undoStack: List[GraphUndoState[Int]],
	adjMatrixState: AdjMatrixInteractionState,
	adjMatrixDimensions: (Int, Int)
)

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
		adjMatrixDimensions = (100, 100) // override in Controller.init after loading settings
	)
}