package graphcontroller.model

import graphcontroller.dataobject.NodeData
import graphi.{DirectedMapGraph, SimpleMapGraph}

case class State(
	graph: DirectedMapGraph[Int] | SimpleMapGraph[Int],
	keyToData: Map[Int, NodeData],
	undoStack: List[GraphUndoState[Int]]
)

object State {
	def init: State = State(
		graph = new DirectedMapGraph[Int](),
		keyToData = Map.empty,
		// it doesn't matter much since the undo stack won't get that big, but the idea of a List for the undo stack
		// was that it has efficient push/pop operations at the front, but because of the stack's limited size, we end
		// up traversing it (O(n)) to drop the oldest state when the limit is reached, which will pretty much happen all
		// the time once a user has been clicking around for a bit ... so maybe a different data structure would be better
		undoStack = List.empty
	)
}