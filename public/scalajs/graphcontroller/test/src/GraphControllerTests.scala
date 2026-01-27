package graphcontroller

import utest.*

import graphcontroller.GraphController
import graphcontroller.dataobject.NodeData

object GraphControllerTests extends TestSuite {
	def tests = Tests {
		test("Test clearing the graph") {
			val controller = new GraphController()
			controller.addNode(1, NodeData(0, 0, 0).toJS)
			controller.addNode(2, NodeData(0, 0, 0).toJS)
			controller.addEdge(1, 2)
			assert(controller.nodeCount() == 2)
			assert(controller.edgeCount() == 1)
			controller.clearGraph()
			assert(controller.nodeCount() == 0)
			assert(controller.edgeCount() == 0)
		}
		test("Test undo with add node and add edge") {
			val controller = new GraphController()

			// save state for undo, then add node 1
			controller.pushUndoState()
			controller.addNode(1, NodeData(0, 0, 0).toJS)
			assert(controller.nodeCount() == 1)

			// ..., then add node 2
			controller.pushUndoState()
			controller.addNode(2, NodeData(0, 0, 0).toJS)
			assert(controller.nodeCount() == 2)

			// ..., then add edge
			controller.pushUndoState()
			controller.addEdge(1, 2)
			assert(controller.nodeCount() == 2)
			assert(controller.edgeCount() == 1)

			// undo adding edge
			controller.popUndoState()
			assert(controller.nodeCount() == 2)
			assert(controller.edgeCount() == 0)

			// undo adding node 2
			controller.popUndoState()
			assert(controller.nodeCount() == 1)
			assert(controller.edgeCount() == 0)

			// undo adding node 1
			controller.popUndoState()
			assert(controller.nodeCount() == 0)
			assert(controller.edgeCount() == 0)
		}
	}
}
