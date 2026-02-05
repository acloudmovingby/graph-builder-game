import graphcontroller.components.exportpane.ExportFormat.Python
import graphcontroller.components.exportpane.ExportPane
import graphcontroller.controller.{AdjMatrixMouseDown, CopyButtonClicked, ExportFormatChanged, NoOp}
import graphcontroller.model.State
import utest.*

object ExportPaneTests extends TestSuite {
	def tests = Tests {

		test("Test that events unrelated to changing format...don't actually change the format") {
			for (event <- Seq(NoOp, CopyButtonClicked, AdjMatrixMouseDown(1, 2))) {
				val newState = ExportPane.update(State.init, NoOp)
				assert(newState.exportFormat == State.init.exportFormat)
			}

			// now change it and assert that further events won't change it back
			// (this seems to be happening while testing live, so adding this unit test to sanity check myself)
			val pythonSelected = ExportPane.update(State.init, ExportFormatChanged(Python))

			for (event <- Seq(NoOp, CopyButtonClicked, AdjMatrixMouseDown(1, 2))) {
				val newState = ExportPane.update(pythonSelected, NoOp)
				assert(newState.exportFormat == Python)
			}
		}
	}
}