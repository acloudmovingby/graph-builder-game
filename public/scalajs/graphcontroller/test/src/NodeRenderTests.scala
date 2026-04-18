import graphcontroller.dataobject.*
import graphcontroller.dataobject.canvas.RectangleCanvas
import graphcontroller.dataobject.NodeData
import graphcontroller.model.HoveredNode
import graphcontroller.shared.{AreaCompleteTool, BuildTool, MagicPathTool, SelectTool}
import utest.*
import graphcontroller.components.maincanvas.MainCanvasView
import graphcontroller.components.maincanvas.NodeRenderStyle.{Basic, BasicHover, Selected}

class NodeRenderTests extends TestSuite {
	override def tests = Tests {
		test("No nodes should return empty list") {
			// Testing all 'valid' permutations of tool types and hover states (given there are zero nodes). So, for example,
			// I'm not testing what happens if there are zero nodes and yet, somehow, state says we're hovering over a node
			for {
				toolState <- Seq(
					BuildTool(None),
					BuildTool(None),
					MagicPathTool(None),
					AreaCompleteTool(true, Nil),
					AreaCompleteTool(true, Vector2D(10, 10) :: Nil)
				)
			} yield {
				val result = MainCanvasView.nodesWithStyles(Seq.empty, None, toolState)
				if (result.nonEmpty) {
					println(s"this failed: $toolState")
				}
				assert(result.isEmpty)
			}
		}

		test("BuildTool: Hovering over 1 node in node-adding mode") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, false)
			val toolState = BuildTool(None) // None indicates we're in node-adding mode (no start of an edge)
			val result = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), toolState)
			assert(result.toSet == Set((0, BasicHover), (1, Basic)))
		}

		test("BuildTool: Hovering over 1 node in node-adding mode (but just added that node)") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, true)
			val toolState = BuildTool(None) // None indicates we're in node-adding mode (no start of an edge)
			val result = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), toolState)
			assert(result.toSet == Set((0, Basic), (1, Basic)))
		}

		test("BuildTool: multiple nodes but no hover") {
			val nodes = Seq(0, 1, 2, 3)
			val result = MainCanvasView.nodesWithStyles(nodes, None, BuildTool(None))
		}

		test("Selected nodes get Selected style regardless of tool state") {
			val nodes = Seq(0, 1, 2)
			val result = MainCanvasView.nodesWithStyles(nodes, None, BuildTool(None), selectedNodes = Set(0, 1)).toMap
			assert(result(0) == Selected)
			assert(result(1) == Selected)
			assert(result(2) == Basic)
		}

		test("Selected style overrides hover") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, false) // node 0 is hovered...
			val result = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), BuildTool(None), selectedNodes = Set(0)).toMap // ...and also selected
			assert(result(0) == Selected) // Selected wins
			assert(result(1) == Basic)
		}

		test("Empty selectedNodes leaves styles unchanged") {
			val nodes = Seq(0, 1)
			val hoveredNode = HoveredNode(0, false)
			val withSel = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), BuildTool(None), selectedNodes = Set.empty)
			val withoutSel = MainCanvasView.nodesWithStyles(nodes, Some(hoveredNode), BuildTool(None))
			assert(withSel == withoutSel)
		}

		// Step 6: bounding box
		test("selectionBoundingBox - empty for 0 selected nodes") {
			val result = MainCanvasView.selectionBoundingBox(Set.empty, Map.empty)
			assert(result.isEmpty)
		}

		test("selectionBoundingBox - empty for 1 selected node") {
			val keyToData = Map(0 -> NodeData(0, 100, 100))
			val result = MainCanvasView.selectionBoundingBox(Set(0), keyToData)
			assert(result.isEmpty)
		}

		test("selectionBoundingBox - encloses all selected nodes with padding") {
			val keyToData = Map(
				0 -> NodeData(0, 10, 10),
				1 -> NodeData(0, 50, 80),
				2 -> NodeData(0, 200, 200) // not selected
			)
			val result = MainCanvasView.selectionBoundingBox(Set(0, 1), keyToData)
			assert(result.size == 1)
			result.head match {
				case rc: RectangleCanvas =>
					assert(rc.rect.topLeft.x < 10)  // left of leftmost node (with padding)
					assert(rc.rect.topLeft.y < 10)  // above topmost node (with padding)
					assert(rc.rect.topLeft.x + rc.rect.width > 50)  // right of rightmost selected node
					assert(rc.rect.topLeft.y + rc.rect.height > 80) // below bottommost selected node
					assert(rc.rect.topLeft.x + rc.rect.width < 200) // node 2 not enclosed
				case _ => assert(false)
			}
		}

		// TODO: BuildTool but in edge adding mode
	}
}
