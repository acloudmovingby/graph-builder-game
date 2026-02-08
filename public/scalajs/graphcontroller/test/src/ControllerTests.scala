import utest.*
import graphcontroller.controller.{AdjMatrixMouseMove, Controller, ExportAdjacencyTypeChanged, ExportFormatChanged, Initialization, NoOp}
import graphcontroller.components.adjacencymatrix.{Hover, NoSelection}
import graphcontroller.components.RenderOp
import graphcontroller.components.exportpane.ExportFormat.Python
import graphcontroller.components.exportpane.ExportPaneRenderData
import graphcontroller.dataobject.{Cell, NoCell}
import graphcontroller.model.State
import graphcontroller.shared.GraphRepresentation
import graphi.SimpleMapGraph

object ControllerTests extends TestSuite {
	def tests = Tests {
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

			val graphWithNodes = new SimpleMapGraph[Int]().addNode(22).addNode(119)
			val with2NodeGraph = newState.copy(graph = graphWithNodes)
			println(s"newState's graph: ${newState.graph.adjMap.keys.mkString(",")}, with2NodeGraph's graph: ${with2NodeGraph.graph.adjMap.keys.mkString(",")}")
			val (newState2, renderOps2) = Controller.handleEventWithState(AdjMatrixMouseMove(50, 50), with2NodeGraph)
			newState2.adjMatrixState match {
				case Hover(Cell(row, col)) => assert(row == 1 && col == 1) // with the default dimensions and padding, mouse at (50, 50) should correspond to cell (5, 5)
				case _ =>
					println("Unexpected adjacency matrix state: " + newState2.adjMatrixState)
					assert(false)
			}
		}
	}
}