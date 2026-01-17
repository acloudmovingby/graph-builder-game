import scala.collection.immutable.ListSet
import utest.*
import graphcontroller.controller.{
	AdjacencyMatrixEvent, AdjMatrixMouseDown, AdjMatrixMouseLeave, AdjMatrixMouseUp, AdjMatrixMouseMove
}
import graphcontroller.model.adjacencymatrix.{
	AdjMatrixClickDragLogic, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection, Cell
}

object AdjMatrixClickDragLogicTests extends TestSuite {
	def tests = Tests {
		val logic = AdjMatrixClickDragLogic
		test("mouseUp") {
			// these three states will result in thrown exception since they should never happen (you can't release a
			// mouse click if you never clicked in the first place)
			Seq(NoSelection, Hover(Cell(0, 0)), ReleaseSelection(Set(Cell(0, 0)), true)).foreach { state =>
				try {
					logic.mouseUp(state)
					assert(false) // should not reach here
				} catch {
					case _: Exception => assert(true) // expected
				}
			}

			// Clicked state
			// isAdd=true
			val clickedState = Clicked(Cell(1, 2), isAdd = true)
			val result1 = logic.mouseUp(clickedState)
			assert(result1 == ReleaseSelection(Set(Cell(1, 2)), isAdd = true))
			// isAdd=false
			val clickedState2 = Clicked(Cell(3, 4), isAdd = false)
			val result2 = logic.mouseUp(clickedState2)
			assert(result2 == ReleaseSelection(Set(Cell(3, 4)), isAdd = false))
		}
		test("mouseup - Releasing when selection is two cells side-by (horizontally)") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X X . .
			// 2 . . . . .
			// 3 . . . . .

			val dragState = DragSelecting(Cell(1, 1), Cell(2, 1), isAdd = true)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(2, 1)), isAdd = true))
		}
		test ("mouseup - when selection is three cells vertically") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . X . . .
			// 3 . X . . .

			val dragState = DragSelecting(Cell(1, 1), Cell(1, 3), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(1, 2), Cell(1, 3)), isAdd = false))
		}
		test ("mouseup - when selection is a single cell") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . . . .
			// 3 . . . . .

			val dragState = DragSelecting(Cell(1, 1), Cell(1, 1), isAdd = true)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(1, 1)), isAdd = true))
		}
		test ("mouseup - when selection is diagonal one square (it defaults to horizontal)") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . X . .
			// 3 . . . . .

			val dragState = DragSelecting(Cell(1, 1), Cell(2, 2), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(2, 1)), isAdd = false))
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

			val dragState = DragSelecting(Cell(1, 1), Cell(3, 2), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(2, 1), Cell(3, 1)), isAdd = false))
		}
		test("mouseup - when selecting self-edges (where from==to)...let caller decide to ignore, return anyway. Default horizontal.") {
			// currently I don't allow self-edges in the graph, but let the caller of this logic decide what to do with them
			// (who knows, someday I'll implement self-edges)

			// ASCII art of the selection but with Y axis labeled "From" and the X axis labeled "To":
			//        To
			//      0 1 2 3 4
			//    0 X . . . .
			// F  1 . . . . .
			// r  2 . . . . .
			// o  3 . . . . .
			// m  4 . . . . X

			val dragState = DragSelecting(Cell(0, 0), Cell(4,4), isAdd = false)
			val result = logic.mouseUp(dragState)
			assert(result == ReleaseSelection(Set(Cell(0, 0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0)), isAdd = false))
		}
		test("hovering over cells within bounds from a NoSelection state") {
			val result = logic.mouseMove(Cell(2, 3), NoSelection, 5)
			assert(result == Hover(Cell(2, 3)))
		}
		test("hovering over cells out of bounds from a NoSelection state") {
			val result1 = logic.mouseMove(Cell(-1, 0), NoSelection, 5)
			assert(result1 == NoSelection)
			val result2 = logic.mouseMove(Cell(0, -1), NoSelection, 5)
			assert(result2 == NoSelection)
			val result3 = logic.mouseMove(Cell(5, 0), NoSelection, 5)
			assert(result3 == NoSelection)
			val result4 = logic.mouseMove(Cell(0, 5), NoSelection, 5)
			assert(result4 == NoSelection)
		}
		test("hovering over cells within bounds from a Hover state") {
			val result = logic.mouseMove(Cell(1, 4), Hover(Cell(2, 3)), 5)
			assert(result == Hover(Cell(1, 4)))
		}
		test("convertMouseCoordinatesToCell") {
			val adjMatrixDimensions = (500, 500)
			val nodeCount = 5
			// cell (0,0)
			val cell1 = logic.convertMouseCoordinatesToCell(0, 0, adjMatrixDimensions, nodeCount)
			assert(cell1 == Cell(0, 0))
			// cell (2,3)
			val cell2 = logic.convertMouseCoordinatesToCell(250, 350, adjMatrixDimensions, nodeCount)
			assert(cell2 == Cell(3, 2))
			// cell (4,4)
			val cell3 = logic.convertMouseCoordinatesToCell(499, 499, adjMatrixDimensions, nodeCount)
			assert(cell3 == Cell(4, 4))
		}
		test("end-to-end handleEvent: NoSelection now Hover") {
			val initialState = NoSelection
			val mouseX = 120
			val mouseY = 80
			val adjMatrixDimensions = (500, 500)
			val nodeCount = 5
			val newState = logic.handleEvent(
				AdjMatrixMouseMove(mouseX, mouseY),
				initialState,
				adjMatrixDimensions,
				nodeCount,
				Set.empty // doesn't matter for this test
			)
			assert(newState == Hover(Cell(0, 1)))
		}
		test("end-to-end handleEvent: Hover now Clicked") {
			val initialState = Hover(Cell(2, 3))
			val (mouseX, mouseY) = (320, 220) // still within cell (2,3)
			val adjMatrixDimensions = (500, 500)
			val nodeCount = 5

			// test with no existing edge
			val filledInCells = Set.empty[Cell]
			val newState = logic.handleEvent(
				AdjMatrixMouseDown(mouseX, mouseY),
				initialState,
				adjMatrixDimensions,
				nodeCount,
				filledInCells
			)
			// since no edge exists, isAdd should be true (we are preparing to add the edge)
			assert(newState == Clicked(Cell(2, 3), isAdd = true))

			// test with an existing edge
			val filledInCells2 = Set(Cell(2, 3))
			val newState2 = logic.handleEvent(
				AdjMatrixMouseDown(mouseX, mouseY),
				initialState,
				adjMatrixDimensions,
				nodeCount,
				filledInCells2
			)
			// since edge exists, isAdd should be false (we are preparing to remove the edge)
			assert(newState2 == Clicked(Cell(2, 3), isAdd = false))
		}
	}
}