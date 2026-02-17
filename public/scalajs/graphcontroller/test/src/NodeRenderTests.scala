import graphcontroller.components.adjacencymatrix.{AdjacencyMatrixView, Hover, NoSelection}
import graphcontroller.dataobject.*
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{AreaCompleteTool, BasicTool, GraphRepresentation, GridUtils, MagicPathTool}
import graphi.DirectedMapGraph
import utest.*
import graphcontroller.components.exportpane.ExportFormat
import graphcontroller.components.maincanvas.MainCanvasView
import graphcontroller.components.maincanvas.NodeRenderStyle.{Basic, BasicHover}

class NodeRenderTests extends TestSuite {
	override def tests = Tests {
		test("No nodes should return empty list") {
			// Testing all 'valid' permutations of tool types and hover states (given there are zero nodes). So, for example,
			// I'm not testing what happens if there are zero nodes and yet, somehow, state says we're hovering over a node
			for {
				toolState <- Seq(
					BasicTool(None),
					BasicTool(None),
					MagicPathTool(None),
					AreaCompleteTool(true, Seq.empty),
					AreaCompleteTool(true, Seq(Vector2D(10, 10)))
				)
			} yield {
				val result = MainCanvasView.nodesWithStyles(Seq.empty, None, toolState)
				if (result.nonEmpty) {
					println(s"this failed: $toolState")
				}
				assert(result.isEmpty)
			}
		}

		test("Basic tool: Hovering over 1 node in node-adding mode") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, false)
			val toolState = BasicTool(None) // None indicates we're in node-adding mode (no start of an edge)
			val result = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), toolState)
			println(s"result=$result")
			assert(result.toSet == Set((0, BasicHover), (1, Basic)))
		}

		test("Basic tool: Hovering over 1 node in node-adding mode (but just added that node)") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, true)
			val toolState = BasicTool(None) // None indicates we're in node-adding mode (no start of an edge)
			val result = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), toolState)
			assert(result.toSet == Set((0, Basic), (1, Basic)))
		}

		test("Basic tool: multiple nodes but no hover") {
			val nodes = Seq(0, 1, 2, 3)
			val result = MainCanvasView.nodesWithStyles(nodes, None, BasicTool(None))
		}

		// TODO: basic tool but in edge adding mode
	}
}
