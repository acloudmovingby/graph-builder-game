package graphcontroller.model

import graphcontroller.dataobject.NodeData
import graphi.MapGraph

// State of the graph to be saved on the undo/redo stacks
case class HistoricalState[A](
	graph: MapGraph[A],
	keyToData: Map[A, NodeData]
)

object HistoricalState {
	// Limit the max number of undo states ... probably should profile this to see if it's necessary / what a good limit is
	val UNDO_SIZE_LIMIT = 50
}
