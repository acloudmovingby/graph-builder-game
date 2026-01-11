import scala.collection.immutable.ListSet
import utest.*
import main.scala.graphcontroller.adjacencymatrix.{AdjMatrixClickDragLogic, AdjMatrixSelectionState, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection}

object AdjMatrixClickDragLogicTests extends TestSuite {
	def tests = Tests {
		test("mouseUp") {
			val logic = new AdjMatrixClickDragLogic()
			// these three states will result in thrown exception since they should never happen (you can't release a
			// mouse click if you never clicked in the first place)
			Seq(NoSelection, Hover((0, 0)), ReleaseSelection(Set((0, 0)), true)).foreach { state =>
				try {
					logic.mouseUp(state)
					assert(false) // should not reach here
				} catch {
					case _: Exception => assert(true) // expected
				}
			}

			// Clicked state
			// isAdd=true
			val clickedState = Clicked((1, 2), isAdd = true)
			val result1 = logic.mouseUp(clickedState)
			assert(result1 == ReleaseSelection(Set((1, 2)), isAdd = true))
			// isAdd=false
			val clickedState2 = Clicked((3, 4), isAdd = false)
			val result2 = logic.mouseUp(clickedState2)
			assert(result2 == ReleaseSelection(Set((3, 4)), isAdd = false))
		}
		test("mouseup - Releasing when selection is two cells side-by (horizontally)") {
			// ASCI I art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X X . .
			// 2 . . . . .
			// 3 . . . . .

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((1, 1), (2, 1), isAdd = true)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((1, 1), (2, 1)), isAdd = true))
		}
		test ("mouseup - when selection is three cells vertically") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . X . . .
			// 3 . X . . .

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((1, 1), (1, 3), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((1, 1), (1, 2), (1, 3)), isAdd = false))
		}
		test ("mouseup - when selection is a single cell") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . . . .
			// 3 . . . . .

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((1, 1), (1, 1), isAdd = true)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((1, 1)), isAdd = true))
		}
		test ("mouseup - when selection is diagonal one square (it defaults to horizontal)") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . X . .
			// 3 . . . . .

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((1, 1), (2, 2), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((1, 1), (2, 1)), isAdd = false))
		}
		test ("mouseup - when selection is knight down right (it defaults to longer delta, the vertical)") {

			// ASCII art of the selection but with Y axis labeled "From" and the X axis labeled "To":
			//        To
			//      0 1 2 3 4
			//    0 . . . . .
			// F  1 . X . . .
			// r  2 . . . . .
			// o  3 . . X . .
			// m  4 . . . . .

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((1, 1), (3, 2), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((1, 1), (2, 1), (3, 1)), isAdd = false))
		}
		test("mouseup - when selecting self-edges (where from==to)...let caller decide to ignore, return anyway. Default horizontal.") {
			// currently I don't allow self-edges in the graph, but let the caller of this logic decide what to do with them
			// (who knows, someday maybe I'll implement self-edges)

			// ASCII art of the selection but with Y axis labeled "From" and the X axis labeled "To":
			//        To
			//      0 1 2 3 4
			//    0 X . . . .
			// F  1 . . . . .
			// r  2 . . . . .
			// o  3 . . . . .
			// m  4 . . . . X

			val logic = new AdjMatrixClickDragLogic()
			val dragState = DragSelecting((0, 0), (4,4), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set((0, 0), (1,0), (2,0), (3,0), (4,0)), isAdd = false))
		}
	}
}