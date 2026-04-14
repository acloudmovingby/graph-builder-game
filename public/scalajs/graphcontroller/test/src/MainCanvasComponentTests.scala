package graphcontroller.components.maincanvas

import utest.*
import graphcontroller.model.State
import graphcontroller.controller.{MainCanvasMouseEvent, MouseEvent}
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.{BasicTool, MagicPathTool, MoveTool, AreaCompleteTool, SelectTool, SelectMode}
import graphcontroller.model.HoveredNode

object MainCanvasComponentTests extends TestSuite {
	def tests = Tests {
		val initState = State.init

		test("BasicTool - add node") {
			val event = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(initState, event)

			assert(stateAfterDown.graph.nodeCount == 1)
			assert(stateAfterDown.keyToData(0).x == 100)
			assert(stateAfterDown.keyToData(0).y == 100)
			assert(stateAfterDown.canvasInteraction.hoveredNode.contains(HoveredNode(0, true)))
		}

		test("BasicTool - hover over node") {
			val stateWithNode = initState.addNode(Vector2D(100, 100))
			val event = MainCanvasMouseEvent(Vector2D(105, 105), MouseEvent.Move)
			val stateAfterMove = MainCanvasComponent.update(stateWithNode, event)

			assert(stateAfterMove.canvasInteraction.hoveredNode.contains(HoveredNode(0, false)))
		}

		test("BasicTool - start edge") {
			val stateWithNode = initState.addNode(Vector2D(100, 100))
			val event = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNode, event)

			assert(stateAfterDown.toolState == BasicTool(Some(0)))
		}

		test("BasicTool - add edge") {
			val stateWithNodes = initState.addNode(Vector2D(100, 100)).addNode(Vector2D(200, 200))
			val stateStartEdge = stateWithNodes.copy(toolState = BasicTool(Some(0)))

			val event = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateStartEdge, event)

			assert(stateAfterDown.graph.edgeCount == 1)
			assert(stateAfterDown.graph.getEdges.contains((0, 1)))
			assert(stateAfterDown.toolState == BasicTool(Some(1)))
		}

		test("MoveTool - drag node") {
			val stateWithNode = initState.addNode(Vector2D(100, 100)).copy(toolState = MoveTool(None))

			// Mouse Down on node
			val downEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNode, downEvent)
			assert(stateAfterDown.toolState == MoveTool(Some(0)))

			// Mouse Move
			val moveEvent = MainCanvasMouseEvent(Vector2D(150, 150), MouseEvent.Move)
			val stateAfterMove = MainCanvasComponent.update(stateAfterDown, moveEvent)
			assert(stateAfterMove.keyToData(0).x == 150)
			assert(stateAfterMove.keyToData(0).y == 150)

			// Mouse Up
			val upEvent = MainCanvasMouseEvent(Vector2D(150, 150), MouseEvent.Up)
			val stateAfterUp = MainCanvasComponent.update(stateAfterMove, upEvent)
			assert(stateAfterUp.toolState == MoveTool(None))
		}

		test("MagicPathTool - auto add edges") {
			val stateWithNodes = initState.addNode(Vector2D(100, 100)).addNode(Vector2D(200, 200)).addNode(Vector2D(300, 300))
			val stateMagic = stateWithNodes.copy(toolState = MagicPathTool(Some(0)))

			val moveEvent = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Move)
			val stateAfterMove = MainCanvasComponent.update(stateMagic, moveEvent)

			assert(stateAfterMove.graph.getEdges.contains((0, 1)))
			assert(stateAfterMove.toolState == MagicPathTool(Some(1)))

			val moveEvent2 = MainCanvasMouseEvent(Vector2D(300, 300), MouseEvent.Move)
			val stateAfterMove2 = MainCanvasComponent.update(stateAfterMove, moveEvent2)

			assert(stateAfterMove2.graph.getEdges.contains((1, 2)))
			assert(stateAfterMove2.toolState == MagicPathTool(Some(2)))
		}

		test("AreaCompleteTool - select multiple nodes") {
			val stateWithNodes = initState
				.addNode(Vector2D(10, 10)) // 0
				.addNode(Vector2D(5, 5)) // 1
				.addNode(Vector2D(100, 100)) // 2 (outside)
				.copy(toolState = AreaCompleteTool(false, Nil))

			// Down at (0,0)
			val downEvent = MainCanvasMouseEvent(Vector2D(0, 0), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNodes, downEvent)

			// Up at (30,30) - currently the isInside check needs more than 2 points to trigger
			// Let's simulate a triangle: (0,0), (30,0), (0,30)
			val stateTriangle = stateAfterDown.copy(toolState = AreaCompleteTool(true, Vector2D(0,0) :: Vector2D(30,0) :: Vector2D(0,30) :: Nil))
			val upEvent = MainCanvasMouseEvent(Vector2D(0, 30), MouseEvent.Up)
			val stateAfterUp = MainCanvasComponent.update(stateTriangle, upEvent)

			assert(stateAfterUp.graph.getEdges.contains((0, 1)))
			assert(stateAfterUp.graph.getEdges.contains((1, 0)))
			assert(!stateAfterUp.graph.getEdges.contains((0, 2)))
		}

		test("SelectTool - drag box lifecycle") {
			val state = initState.copy(toolState = SelectTool())

			// Down on empty canvas starts dragging a box
			val downEvent = MainCanvasMouseEvent(Vector2D(10, 10), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(state, downEvent)
			assert(stateAfterDown.toolState == SelectTool(SelectMode.DraggingBox(Vector2D(10, 10))))
			assert(stateAfterDown.selectedNodes.isEmpty)

			// Up finalises the selection (no nodes, so empty set)
			val upEvent = MainCanvasMouseEvent(Vector2D(50, 50), MouseEvent.Up)
			val stateAfterUp = MainCanvasComponent.update(stateAfterDown, upEvent)
			assert(stateAfterUp.toolState == SelectTool(SelectMode.Idle))
			assert(stateAfterUp.selectedNodes.isEmpty)
		}

		test("SelectTool - rectangle selects nodes inside") {
			val stateWithNodes = initState
				.addNode(Vector2D(20, 20)) // 0 - inside rect
				.addNode(Vector2D(40, 40)) // 1 - inside rect
				.addNode(Vector2D(200, 200)) // 2 - outside rect
				.copy(toolState = SelectTool(SelectMode.DraggingBox(Vector2D(0, 0))))

			val upEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Up)
			val stateAfterUp = MainCanvasComponent.update(stateWithNodes, upEvent)
			assert(stateAfterUp.selectedNodes == Set(0, 1))
			assert(!stateAfterUp.selectedNodes.contains(2))
		}

		test("SelectTool - rectangle works when dragged in reverse direction") {
			val stateWithNode = initState
				.addNode(Vector2D(50, 50)) // 0 - inside rect
				.copy(toolState = SelectTool(SelectMode.DraggingBox(Vector2D(100, 100)))) // drag started bottom-right

			val upEvent = MainCanvasMouseEvent(Vector2D(0, 0), MouseEvent.Up) // released top-left
			val stateAfterUp = MainCanvasComponent.update(stateWithNode, upEvent)
			assert(stateAfterUp.selectedNodes == Set(0))
		}

		test("SelectTool - clicking a node selects just that node") {
			val stateWithNodes = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool())

			val downEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNodes, downEvent)
			assert(stateAfterDown.selectedNodes == Set(0))
			assert(stateAfterDown.toolState == SelectTool(SelectMode.Idle))
		}

		test("SelectTool - clicking empty canvas clears selection") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100)) // 0
				.copy(toolState = SelectTool(), selectedNodes = Set(0))

			// Click far away from any node
			val downEvent = MainCanvasMouseEvent(Vector2D(500, 500), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithSelection, downEvent)
			assert(stateAfterDown.selectedNodes.isEmpty)
		}

		test("Leave event resets tool state") {
			val stateStartEdge = initState.addNode(Vector2D(100, 100)).copy(toolState = BasicTool(Some(0)))
			val leaveEvent = MainCanvasMouseEvent(Vector2D(500, 500), MouseEvent.Leave)
			val stateAfterLeave = MainCanvasComponent.update(stateStartEdge, leaveEvent)

			assert(stateAfterLeave.toolState == BasicTool(None))
		}
	}
}
