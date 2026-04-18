package graphcontroller.components.maincanvas

import graphcontroller.controller.MouseEvent.DoubleClick
import utest.*
import graphcontroller.model.{HoveredNode, MainCanvasInteractionState, State}
import graphcontroller.controller.{MainCanvasMouseEvent, MouseEvent}
import graphcontroller.dataobject.Vector2D
import graphcontroller.controller.{CompleteSelectedEdges, DeleteSelectedNodes}
import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, SelectMode, SelectTool}

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

		test("SelectTool - clicking a node selects it and enters DraggingNodes") {
			val stateWithNodes = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool())

			val downEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val stateAfterDown = MainCanvasComponent.update(stateWithNodes, downEvent)
			assert(stateAfterDown.selectedNodes == Set(0))
			assert(stateAfterDown.toolState == SelectTool(SelectMode.DraggingNodes(Vector2D(100, 100))))

			// On release without movement, returns to Idle
			val upEvent = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Up)
			val stateAfterUp = MainCanvasComponent.update(stateAfterDown, upEvent)
			assert(stateAfterUp.toolState == SelectTool(SelectMode.Idle))
			assert(stateAfterUp.selectedNodes == Set(0)) // selection preserved
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

		// Node dragging
		test("SelectTool - click and drag a single node moves it") {
			val stateWithNode = initState
				.addNode(Vector2D(100, 100)) // 0
				.copy(toolState = SelectTool())

			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(stateWithNode, down)
			assert(s1.selectedNodes == Set(0))
			assert(s1.toolState == SelectTool(SelectMode.DraggingNodes(Vector2D(100, 100))))

			val move = MainCanvasMouseEvent(Vector2D(150, 120), MouseEvent.Move)
			val s2 = MainCanvasComponent.update(s1, move)
			assert(s2.keyToData(0).x == 150)
			assert(s2.keyToData(0).y == 120)
			assert(s2.toolState == SelectTool(SelectMode.DraggingNodes(Vector2D(150, 120), hasMoved = true)))

			val up = MainCanvasMouseEvent(Vector2D(150, 120), MouseEvent.Up)
			val s3 = MainCanvasComponent.update(s2, up)
			assert(s3.toolState == SelectTool(SelectMode.Idle))
			assert(s3.keyToData(0).x == 150) // position kept
		}

		test("SelectTool - dragging a selected node moves all selected nodes together") {
			val stateWithNodes = initState
				.addNode(Vector2D(100, 100)) // 0 - selected
				.addNode(Vector2D(200, 200)) // 1 - selected
				.addNode(Vector2D(400, 400)) // 2 - NOT selected
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1))

			// Down on node 0 (already selected) — should keep both selected and start drag
			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(stateWithNodes, down)
			assert(s1.selectedNodes == Set(0, 1))
			assert(s1.toolState == SelectTool(SelectMode.DraggingNodes(Vector2D(100, 100))))

			// Move by +50, +30
			val move = MainCanvasMouseEvent(Vector2D(150, 130), MouseEvent.Move)
			val s2 = MainCanvasComponent.update(s1, move)
			assert(s2.keyToData(0).x == 150)
			assert(s2.keyToData(0).y == 130)
			assert(s2.keyToData(1).x == 250)
			assert(s2.keyToData(1).y == 230)
			assert(s2.keyToData(2).x == 400) // unselected node unchanged
			assert(s2.keyToData(2).y == 400)
		}

		test("SelectTool - clicking an unselected node while others are selected replaces selection and drags") {
			val stateWithNodes = initState
				.addNode(Vector2D(100, 100)) // 0 - was selected
				.addNode(Vector2D(300, 300)) // 1 - will be clicked
				.copy(toolState = SelectTool(), selectedNodes = Set(0))

			val down = MainCanvasMouseEvent(Vector2D(300, 300), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(stateWithNodes, down)
			// Should deselect node 0 and select/drag node 1
			assert(s1.selectedNodes == Set(1))
			assert(s1.toolState == SelectTool(SelectMode.DraggingNodes(Vector2D(300, 300))))
		}

		test("SelectTool - drag is undoable (pushes undo state on first Move)") {
			val stateWithNode = initState.addNode(Vector2D(100, 100)).copy(toolState = SelectTool())
			val undoStackSizeBefore = stateWithNode.undoStack.size

			// Down alone should NOT push undo
			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(stateWithNode, down)
			assert(s1.undoStack.size == undoStackSizeBefore)

			// First Move pushes undo
			val move = MainCanvasMouseEvent(Vector2D(110, 110), MouseEvent.Move)
			val s2 = MainCanvasComponent.update(s1, move)
			assert(s2.undoStack.size == undoStackSizeBefore + 1)

			// Second Move does NOT push undo again
			val move2 = MainCanvasMouseEvent(Vector2D(120, 120), MouseEvent.Move)
			val s3 = MainCanvasComponent.update(s2, move2)
			assert(s3.undoStack.size == undoStackSizeBefore + 1)
		}

		// Step 5: live preview
		test("SelectTool - live preview updates selectedNodes on Move during DraggingBox") {
			val stateWithNodes = initState
				.addNode(Vector2D(30, 30)) // 0 - will be inside small rect
				.addNode(Vector2D(200, 200)) // 1 - outside
				.copy(toolState = SelectTool(SelectMode.DraggingBox(Vector2D(0, 0))))

			val moveEvent = MainCanvasMouseEvent(Vector2D(80, 80), MouseEvent.Move)
			val stateAfterMove = MainCanvasComponent.update(stateWithNodes, moveEvent)
			assert(stateAfterMove.selectedNodes == Set(0))
			assert(!stateAfterMove.selectedNodes.contains(1))
		}

		test("SelectTool - live preview shrinks when box shrinks") {
			val stateWithNodes = initState
				.addNode(Vector2D(30, 30))   // 0
				.addNode(Vector2D(150, 150)) // 1
				.copy(toolState = SelectTool(SelectMode.DraggingBox(Vector2D(0, 0))))

			// First move: both nodes inside
			val move1 = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Move)
			val stateAfterMove1 = MainCanvasComponent.update(stateWithNodes, move1)
			assert(stateAfterMove1.selectedNodes == Set(0, 1))

			// Second move: box shrinks, only node 0 inside
			val stateForMove2 = stateAfterMove1.copy(toolState = SelectTool(SelectMode.DraggingBox(Vector2D(0, 0))))
			val move2 = MainCanvasMouseEvent(Vector2D(80, 80), MouseEvent.Move)
			val stateAfterMove2 = MainCanvasComponent.update(stateForMove2, move2)
			assert(stateAfterMove2.selectedNodes == Set(0))
		}

		test("SelectTool - shift+drag adds newly boxed nodes to existing selection") {
			val stateWithSelection = initState
				.addNode(Vector2D(50, 50))   // 0 - already selected
				.addNode(Vector2D(300, 300)) // 1 - will be drag-selected with shift
				.addNode(Vector2D(400, 400)) // 2 - outside both selections
				.copy(toolState = SelectTool(), selectedNodes = Set(0))

			// Shift+mousedown on empty canvas far from node 0 — should keep node 0 selected
			val downEvent = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down, shiftKey = true)
			val stateAfterDown = MainCanvasComponent.update(stateWithSelection, downEvent)
			assert(stateAfterDown.selectedNodes == Set(0)) // existing selection preserved

			// Move to include node 1 in the box
			val moveEvent = MainCanvasMouseEvent(Vector2D(380, 380), MouseEvent.Move, shiftKey = true)
			val stateAfterMove = MainCanvasComponent.update(stateAfterDown, moveEvent)
			assert(stateAfterMove.selectedNodes == Set(0, 1)) // node 0 still selected + node 1 previewed

			// Release — finalise: both 0 and 1 should be selected
			val upEvent = MainCanvasMouseEvent(Vector2D(380, 380), MouseEvent.Up, shiftKey = true)
			val stateAfterUp = MainCanvasComponent.update(stateAfterMove, upEvent)
			assert(stateAfterUp.selectedNodes == Set(0, 1))
			assert(!stateAfterUp.selectedNodes.contains(2))
		}

		// Step 7: delete
		test("DeleteSelectedNodes clears selectedNodes") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100))
				.copy(toolState = SelectTool(), selectedNodes = Set(0))
			assert(stateWithSelection.selectedNodes.nonEmpty)
			val newState = MainCanvasComponent.update(stateWithSelection, DeleteSelectedNodes)
			assert(newState.selectedNodes.isEmpty)
			assert(newState.graph.nodeCount == 0)
		}

		test("DeleteSelectedNodes with empty selection is a no-op") {
			val state = initState.addNode(Vector2D(100, 100)).copy(toolState = SelectTool())
			val newState = MainCanvasComponent.update(state, DeleteSelectedNodes)
			assert(newState.selectedNodes.isEmpty)
			assert(newState.graph.nodeCount == 1) // graph unchanged
		}

		test("DeleteSelectedNodes clears multiple selectedNodes") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100))
				.addNode(Vector2D(200, 200))
				.addNode(Vector2D(300, 300))
				.addNode(Vector2D(400, 400))
				.copy(
					toolState = SelectTool(),
					selectedNodes = Set(1, 2),
					canvasInteraction = MainCanvasInteractionState(Some(HoveredNode(1, false)), Vector2D(0, 0))
				)
			assert(stateWithSelection.selectedNodes.nonEmpty)
			val newState = MainCanvasComponent.update(stateWithSelection, DeleteSelectedNodes)
			assert(newState.selectedNodes.isEmpty)
			assert(newState.graph.nodeCount == 2)
			assert(newState.graph.nodes.toSet == Set(0, 3))
			println(s"OLD KEYTODATA: ${stateWithSelection.keyToData}")
			println(s"NEW KEYTODATA: ${newState.keyToData}")
			assert(newState.keyToData.keys.toSet == Set(0,3))
			assert(newState.canvasInteraction.hoveredNode.isEmpty)
		}

		// Step 8: shift+click
		test("SelectTool - shift+click adds unselected node to selection") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool(), selectedNodes = Set(0))

			val shiftClick = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down, shiftKey = true)
			val newState = MainCanvasComponent.update(stateWithSelection, shiftClick)
			assert(newState.selectedNodes == Set(0, 1))
		}

		test("SelectTool - shift+click removes already-selected node from selection") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1))

			val shiftClick = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down, shiftKey = true)
			val newState = MainCanvasComponent.update(stateWithSelection, shiftClick)
			assert(newState.selectedNodes == Set(0))
		}

		test("SelectTool - normal click still replaces selection") {
			val stateWithSelection = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool(), selectedNodes = Set(0))

			val normalClick = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down, shiftKey = false)
			val newState = MainCanvasComponent.update(stateWithSelection, normalClick)
			assert(newState.selectedNodes == Set(1))
		}

		// Step 9: double-click
		test("SelectTool - double-click on empty canvas adds node") {
			val state = initState.copy(toolState = SelectTool())
			val dblClick = MainCanvasMouseEvent(Vector2D(150, 150), DoubleClick)
			val newState = MainCanvasComponent.update(state, dblClick)
			assert(newState.graph.nodeCount == 1)
			assert(newState.keyToData(0).x == 150)
			assert(newState.keyToData(0).y == 150)
			assert(newState.toolState.isInstanceOf[SelectTool]) // stays in SelectTool
		}

		test("SelectTool - double-click on existing node is a no-op") {
			val stateWithNode = initState.addNode(Vector2D(100, 100)).copy(toolState = SelectTool())
			val dblClick = MainCanvasMouseEvent(Vector2D(100, 100), DoubleClick)
			val newState = MainCanvasComponent.update(stateWithNode, dblClick)
			assert(newState.graph.nodeCount == 1) // unchanged
		}

		test("Double-click in BasicTool is a no-op") {
			val state = initState.copy(toolState = BasicTool(None))
			val dblClick = MainCanvasMouseEvent(Vector2D(100, 100), DoubleClick)
			val newState = MainCanvasComponent.update(state, dblClick)
			assert(newState.graph.nodeCount == 0) // unchanged
		}

		test("Leave event resets tool state") {
			val stateStartEdge = initState.addNode(Vector2D(100, 100)).copy(toolState = BasicTool(Some(0)))
			val leaveEvent = MainCanvasMouseEvent(Vector2D(500, 500), MouseEvent.Leave)
			val stateAfterLeave = MainCanvasComponent.update(stateStartEdge, leaveEvent)

			assert(stateAfterLeave.toolState == BasicTool(None))
		}

		// Feature 1: CompleteSelectedEdges
		test("CompleteSelectedEdges with 3 selected nodes adds all edges between them") {
			val state = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.addNode(Vector2D(300, 300)) // 2
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1, 2))
			val newState = MainCanvasComponent.update(state, CompleteSelectedEdges)
			assert(newState.graph.getEdges.contains((0, 1)))
			assert(newState.graph.getEdges.contains((1, 0)))
			assert(newState.graph.getEdges.contains((0, 2)))
			assert(newState.graph.getEdges.contains((2, 0)))
			assert(newState.graph.getEdges.contains((1, 2)))
			assert(newState.graph.getEdges.contains((2, 1)))
		}

		test("CompleteSelectedEdges with 1 selected node is a no-op") {
			val state = initState
				.addNode(Vector2D(100, 100))
				.copy(toolState = SelectTool(), selectedNodes = Set(0))
			val newState = MainCanvasComponent.update(state, CompleteSelectedEdges)
			assert(newState.graph.edgeCount == 0)
			assert(newState.undoStack == state.undoStack)
		}

		test("CompleteSelectedEdges with 0 selected nodes is a no-op") {
			val state = initState
				.addNode(Vector2D(100, 100))
				.copy(toolState = SelectTool(), selectedNodes = Set.empty)
			val newState = MainCanvasComponent.update(state, CompleteSelectedEdges)
			assert(newState.graph.edgeCount == 0)
			assert(newState.undoStack == state.undoStack)
		}

		test("CompleteSelectedEdges is undoable") {
			import graphcontroller.controller.{Controller, UndoRequested}
			val state = initState
				.addNode(Vector2D(100, 100))
				.addNode(Vector2D(200, 200))
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1))
			val initUndoStackSize = state.undoStack.size
			val stateAfterComplete = MainCanvasComponent.update(state, CompleteSelectedEdges)
			assert(stateAfterComplete.graph.edgeCount == 2)
			assert(stateAfterComplete.undoStack.size == initUndoStackSize + 1)
			val (stateAfterUndo, _) = Controller.handleEventWithState(UndoRequested, stateAfterComplete)
			assert(stateAfterUndo.graph.edgeCount == 0)
			assert(stateAfterUndo.undoStack.size == initUndoStackSize)
		}

		test("CompleteSelectedEdges is idempotent") {
			val state = initState
				.addNode(Vector2D(100, 100))
				.addNode(Vector2D(200, 200))
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1))
			val s1 = MainCanvasComponent.update(state, CompleteSelectedEdges)
			val s2 = MainCanvasComponent.update(s1, CompleteSelectedEdges)
			assert(s1.graph.edgeCount == s2.graph.edgeCount)
		}

		// Feature 2: Click-to-narrow selection
		test("SelectTool - click (no move) on node in multi-selection narrows to that node") {
			val state = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.addNode(Vector2D(300, 300)) // 2
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1, 2))

			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(state, down)
			assert(s1.selectedNodes == Set(0, 1, 2)) // selection unchanged during Down

			val up = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Up)
			val s2 = MainCanvasComponent.update(s1, up)
			assert(s2.selectedNodes == Set(0)) // narrowed to clicked node
			assert(s2.toolState == SelectTool(SelectMode.Idle))
		}

		test("SelectTool - drag (Down+Move+Up) on node in multi-selection preserves full selection") {
			val state = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.addNode(Vector2D(300, 300)) // 2
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1, 2))

			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val s1 = MainCanvasComponent.update(state, down)

			val move = MainCanvasMouseEvent(Vector2D(110, 110), MouseEvent.Move)
			val s2 = MainCanvasComponent.update(s1, move)

			val up = MainCanvasMouseEvent(Vector2D(110, 110), MouseEvent.Up)
			val s3 = MainCanvasComponent.update(s2, up)
			assert(s3.selectedNodes == Set(0, 1, 2)) // all three still selected
		}

		test("SelectTool - click-to-narrow does NOT push undo state") {
			val state = initState
				.addNode(Vector2D(100, 100)) // 0
				.addNode(Vector2D(200, 200)) // 1
				.copy(toolState = SelectTool(), selectedNodes = Set(0, 1))
			val undoStackSizeBefore = state.undoStack.size

			val down = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Down)
			val up = MainCanvasMouseEvent(Vector2D(100, 100), MouseEvent.Up)
			val s1 = MainCanvasComponent.update(state, down)
			val s2 = MainCanvasComponent.update(s1, up)
			assert(s2.undoStack.size == undoStackSizeBefore)
		}
	}
}
