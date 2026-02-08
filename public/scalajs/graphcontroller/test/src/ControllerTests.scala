import utest.*

import graphcontroller.controller.{AdjMatrixMouseMove, Controller, ExportFormatChanged, Initialization, NoOp}
import graphcontroller.components.exportpane.ExportFormat.Python
import graphcontroller.model.State

object ControllerTests extends TestSuite {
	// TODO: if/when we make Controller's render more pure fuction, instead of tasting just updateState, do the
	// full handleEvent cycle
	def tests = Tests {
		test("Initialization") {
			val newState = Controller.updateState(Initialization(100, 100, 10, 5), State.init)
			assert(newState.adjMatrixDimensions.canvasWidth == 100)
			assert(newState.adjMatrixDimensions.canvasHeight == 100)
			assert(newState.adjMatrixDimensions.padding == 10)
			assert(newState.adjMatrixDimensions.numberPadding == 5)
		}
		test("Change export format") {
			val newState = Controller.updateState(ExportFormatChanged(Python), State.init)
				// assert that the export format is updated
			assert(newState.exportFormat == Python)
		}
		test("Hover over adjacency matrix") {
			// just assert that the hover state is updated with the mouse position
			Controller.updateState(AdjMatrixMouseMove(50, 50), State.init)
		}
	}
}