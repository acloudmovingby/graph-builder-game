import utest.*
import graphcontroller.controller.{AdjMatrixMouseMove, ClearButtonClicked, Controller, ExportAdjacencyTypeChanged, ExportFormatChanged, Initialization, NoOp, ToggleLabelsVisibility, UndoRequested}
import graphcontroller.components.adjacencymatrix.{Hover, NoSelection}
import graphcontroller.components.buildpane.BuildPaneRenderOp
import graphcontroller.components.RenderOp
import graphcontroller.components.exportpane.ExportFormat.Python
import graphcontroller.components.exportpane.ExportPaneRenderData
import graphcontroller.components.ops.{RemoveAttribute, SetAttribute}
import graphcontroller.components.undobutton.UndoViewData
import graphcontroller.dataobject.{Cell, Vector2D}
import graphcontroller.model.State
import graphcontroller.shared.GraphRepresentation

object ControllerTests extends TestSuite {
	def tests = Tests {
		test("Undo") {
			val stateWithNode = State.init.addNode(Vector2D(10, 10))
			assert(stateWithNode.graph.nodeCount == 1)
			assert(stateWithNode.undoStack.size == 1)

			val (stateAfterUndo, renderOps) = Controller.handleEventWithState(UndoRequested, stateWithNode)
			assert(stateAfterUndo.graph.nodeCount == 0)
			assert(stateAfterUndo.undoStack.isEmpty)

			// Check that the undo button is disabled
			val undoOp = renderOps.collectFirst {
				case op: UndoViewData => op
			}
			assert(undoOp.isDefined)
			assert(!undoOp.get.canUndo)
		}

		test("Undo button enablement") {
			val (stateWithNode, _) = Controller.handleEventWithState(NoOp, State.init.addNode(Vector2D(10, 10)))
			val (_, renderOps) = Controller.handleEventWithState(NoOp, stateWithNode)
			
			// Check that the undo button is enabled
			val undoOp = renderOps.collectFirst {
				case op: UndoViewData => op
			}
			assert(undoOp.isDefined)
			assert(undoOp.get.canUndo)
		}

		test("Initialization") {
			val initEvent = Initialization(100, 100, 10, 5)

			val (newState, renderOps) = Controller.handleEventWithState(initEvent, State.init)
			assert(newState.adjMatrixDimensions.canvasWidth == 100)
			assert(newState.adjMatrixDimensions.canvasHeight == 100)
			assert(newState.adjMatrixDimensions.padding == 10)
			assert(newState.adjMatrixDimensions.numberPadding == 5)
			assert(renderOps.nonEmpty) // TODO maybe check the adjacency matrix render op is correct
		}

		test("Test export pane events") {
			def getExportPaneData(renderOps: Seq[RenderOp]): Option[(Boolean, String, Option[String])] = {
				renderOps.flatMap {
					case ExportPaneRenderData(description, shouldShowGraphReps, preview, clipboard) =>
						Some((shouldShowGraphReps, preview, clipboard))
					case _ => None
				}.headOption
			}

			// change to Python format
			val (newState, renderOps) = Controller.handleEventWithState(ExportFormatChanged(Python), State.init)
			assert(newState.exportFormat == Python)
			val exportPaneData = getExportPaneData(renderOps)

			assert(exportPaneData.nonEmpty)
			exportPaneData.foreach { case (shouldShowGraphReps, preview, clipboard) =>
				assert(shouldShowGraphReps) // should show graph representations for Python
				assert(preview == "{}") // the preview should be an empty list for an empty graph
				assert(clipboard.isEmpty) // clipboard content should be empty until the user clicks the copy button
			}

			// change to matrix format
			val (newState2, renderOps2) = Controller.handleEventWithState(ExportAdjacencyTypeChanged(GraphRepresentation.Matrix), newState)
			val exportPaneData2 = getExportPaneData(renderOps2)
			exportPaneData2.foreach { case (shouldShowGraphReps, preview, clipboard) =>
				assert(shouldShowGraphReps)
				assert(preview == "[]") // Array for matrix representation
				assert(clipboard.isEmpty)
			}
		}

		test("Hover over adjacency matrix") {
			// just assert that the hover state is updated with the mouse position
			val (newState, renderOps) = Controller.handleEventWithState(AdjMatrixMouseMove(50, 50), State.init)
			newState.adjMatrixState match {
				case NoSelection =>
					// no cells since the graph is empty.
					// TODO arguably this could be Hover(NoCell) instead of NoSelection but I'm not gonna change it at the moment in this commit
					assert(true)
				case _ =>
					println("Unexpected adjacency matrix state: " + newState.adjMatrixState)
					assert(false)
			}

			val (newState2, renderOps2) = Controller.handleEventWithState(
				AdjMatrixMouseMove(50, 50),
				newState.addNode(Vector2D(0, 0)).addNode(Vector2D(10, 10))
			)
			newState2.adjMatrixState match {
				case Hover(Cell(row, col)) => assert(row == 1 && col == 1)
				case _ =>
					println("Unexpected adjacency matrix state: " + newState2.adjMatrixState)
					assert(false)
			}
		}

		test("handleEventWithState with ToggleLabelsVisibility should be reversible") {
			def findVisibleIconRenderOp(renderOps: Seq[RenderOp]): Option[SetAttribute] = {
				renderOps.collectFirst {
					case BuildPaneRenderOp(ops) => ops.collectFirst {
						case op: SetAttribute if op.elementId == "visible-icon" => op
					}
				}.flatten
			}

			// === Step 1: Toggle OFF ===
			val initialState = State.init
			assert(initialState.labelsVisible)

			val (stateAfterToggleOff, renderOpsOff) = Controller.handleEventWithState(ToggleLabelsVisibility, initialState)

			// Assert state is OFF
			assert(!stateAfterToggleOff.labelsVisible)

			// Assert RenderOp is for "closed eye" icon
			val setAttrOpOff = findVisibleIconRenderOp(renderOpsOff)

			assert(setAttrOpOff.isDefined)
			setAttrOpOff.foreach { op =>
				assert(op.attribute == "src")
				assert(op.value == "images/invisible-icon.svg")
			}

			// === Step 2: Toggle ON ===
			val (stateAfterToggleOn, renderOpsOn) = Controller.handleEventWithState(ToggleLabelsVisibility, stateAfterToggleOff)

			// Assert state is ON
			assert(stateAfterToggleOn.labelsVisible)

			// Assert RenderOp is for "open eye" icon
			val setAttrOpOn = findVisibleIconRenderOp(renderOpsOn)
			assert(setAttrOpOn.isDefined)
			setAttrOpOn.foreach { op =>
				assert(op.attribute == "src")
				assert(op.value == "images/node-label-visible.svg")
			}
		}

		test("Clear graph is undoable") {
			val stateWithNodes = State.init
				.addNode(Vector2D(10, 10))
				.addNode(Vector2D(20, 20))
			assert(stateWithNodes.graph.nodeCount == 2)
			val undoStackSizeBeforeClear = stateWithNodes.undoStack.size

			val (stateAfterClear, _) = Controller.handleEventWithState(ClearButtonClicked, stateWithNodes)
			assert(stateAfterClear.graph.nodeCount == 0)
			assert(stateAfterClear.undoStack.size == undoStackSizeBeforeClear + 1)

			val (stateAfterUndo, _) = Controller.handleEventWithState(UndoRequested, stateAfterClear)
			assert(stateAfterUndo.graph.nodeCount == 2)
		}

		test("bulkUpdateEdges pushes single undo state only if changed") {
			val initialState = State.init
				.addNode(Vector2D(10, 10))
				.addNode(Vector2D(20, 20)) // node 0 and node 1
			val baseUndoStackSize = initialState.undoStack.size

			// 1. Add multiple edges
			val cellsToAdd = Seq((0, 1), (1, 0))
			val stateWithEdges = initialState.bulkUpdateEdges(cellsToAdd, isAdd = true)
			assert(stateWithEdges.graph.getEdges.size == 2)
			assert(stateWithEdges.undoStack.size == baseUndoStackSize + 1)

			// 2. Try adding same edges again (no change)
			val stateNoChange = stateWithEdges.bulkUpdateEdges(cellsToAdd, isAdd = true)
			assert(stateNoChange.undoStack.size == stateWithEdges.undoStack.size)
			assert(stateNoChange eq stateWithEdges)

			// 3. Remove multiple edges
			val stateAfterRemove = stateWithEdges.bulkUpdateEdges(cellsToAdd, isAdd = false)
			assert(stateAfterRemove.graph.getEdges.isEmpty)
			assert(stateAfterRemove.undoStack.size == stateWithEdges.undoStack.size + 1)
		}
	}
}