package graphcontroller.components.maincanvas

import utest.*
import graphcontroller.model.State
import graphcontroller.controller.{MainCanvasMouseEvent, MouseEventType}
import graphcontroller.dataobject.Vector2D
import graphcontroller.shared.{BasicTool, MagicPathTool, MoveTool, AreaCompleteTool}
import graphcontroller.model.HoveredNode

object MainCanvasComponentTests extends TestSuite {
	def tests = Tests {
		val initState = State.init

		test("BasicTool - add node") {
			val event = MainCanvasMouseEvent(Vector2D(100, 100), MouseEventType.Down)
			val stateAfterDown = MainCanvasComponent.update(initState, event)

			assert(stateAfterDown.graph.nodeCount == 1)
			assert(stateAfterDown.keyToData(0).x == 100)
			assert(stateAfterDown.keyToData(0).y == 100)
			assert(stateAfterDown.hoveringOnNode.contains(HoveredNode(0, true)))
		}

		test("BasicTool - hover over node") {
			val stateWithNode = initState.addNode(Vector2D(100, 100))
			val event = MainCanvasMouseEvent(Vector2D(105, 105), MouseEventType.Move)
			val stateAfterMove = MainCanvasComponent.update(stateWithNode, event)

			assert(stateAfterMove.hoveringOnNode.contains(HoveredNode(0, false)))
		}

		test("BasicTool - start edge") {
			val stateWithNode = initState.addNode(Vector2D(100, 100))
			val event = MainCanvasMouseEvent(Vector2D(100, 100), MouseEventType.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNode, event)

			assert(stateAfterDown.toolState == BasicTool(Some(0)))
		}

		test("BasicTool - add edge") {
			val stateWithNodes = initState.addNode(Vector2D(100, 100)).addNode(Vector2D(200, 200))
			val stateStartEdge = stateWithNodes.copy(toolState = BasicTool(Some(0)))

			val event = MainCanvasMouseEvent(Vector2D(200, 200), MouseEventType.Down)
			val stateAfterDown = MainCanvasComponent.update(stateStartEdge, event)

			assert(stateAfterDown.graph.edgeCount == 1)
			assert(stateAfterDown.graph.getEdges.contains((0, 1)))
			assert(stateAfterDown.toolState == BasicTool(Some(1)))
		}

		test("MoveTool - drag node") {
			val stateWithNode = initState.addNode(Vector2D(100, 100)).copy(toolState = MoveTool(None))

			// Mouse Down on node
			val downEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEventType.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNode, downEvent)
			assert(stateAfterDown.toolState == MoveTool(Some(0)))

			// Mouse Move
			val moveEvent = MainCanvasMouseEvent(Vector2D(150, 150), MouseEventType.Move)
			val stateAfterMove = MainCanvasComponent.update(stateAfterDown, moveEvent)
			assert(stateAfterMove.keyToData(0).x == 150)
			assert(stateAfterMove.keyToData(0).y == 150)

			// Mouse Up
			val upEvent = MainCanvasMouseEvent(Vector2D(150, 150), MouseEventType.Up)
			val stateAfterUp = MainCanvasComponent.update(stateAfterMove, upEvent)
			assert(stateAfterUp.toolState == MoveTool(None))
		}

		test("MagicPathTool - auto add edges") {
			val stateWithNodes = initState.addNode(Vector2D(100, 100)).addNode(Vector2D(200, 200)).addNode(Vector2D(300, 300))
			val stateMagic = stateWithNodes.copy(toolState = MagicPathTool(Some(0)))

			val moveEvent = MainCanvasMouseEvent(Vector2D(200, 200), MouseEventType.Move)
			val stateAfterMove = MainCanvasComponent.update(stateMagic, moveEvent)

			assert(stateAfterMove.graph.getEdges.contains((0, 1)))
			assert(stateAfterMove.toolState == MagicPathTool(Some(1)))

			val moveEvent2 = MainCanvasMouseEvent(Vector2D(300, 300), MouseEventType.Move)
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
			val downEvent = MainCanvasMouseEvent(Vector2D(0, 0), MouseEventType.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNodes, downEvent)

			// Up at (30,30) - currently the isInside check needs more than 2 points to trigger
			// Let's simulate a triangle: (0,0), (30,0), (0,30)
			val stateTriangle = stateAfterDown.copy(toolState = AreaCompleteTool(true, Vector2D(0,0) :: Vector2D(30,0) :: Vector2D(0,30) :: Nil))
			val upEvent = MainCanvasMouseEvent(Vector2D(0, 30), MouseEventType.Up)
			val stateAfterUp = MainCanvasComponent.update(stateTriangle, upEvent)

			assert(stateAfterUp.graph.getEdges.contains((0, 1)))
			assert(stateAfterUp.graph.getEdges.contains((1, 0)))
			assert(!stateAfterUp.graph.getEdges.contains((0, 2)))
		}

		test("Leave event resets tool state") {
			val stateStartEdge = initState.addNode(Vector2D(100, 100)).copy(toolState = BasicTool(Some(0)))
			val leaveEvent = MainCanvasMouseEvent(Vector2D(500, 500), MouseEventType.Leave)
			val stateAfterLeave = MainCanvasComponent.update(stateStartEdge, leaveEvent)

			assert(stateAfterLeave.toolState == BasicTool(None))
		}
	}
}
