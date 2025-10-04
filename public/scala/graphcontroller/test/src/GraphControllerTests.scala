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
	}
}
