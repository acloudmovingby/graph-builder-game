import scala.collection.immutable.ListSet
import utest.*
import graphcontroller.controller.{
	AdjacencyMatrixEvent, AdjMatrixMouseDown, AdjMatrixMouseLeave, AdjMatrixMouseUp, AdjMatrixMouseMove
}
import graphcontroller.dataobject.{Cell, Column, NoCell, Row}
import graphcontroller.model.adjacencymatrix.{
	AdjMatrixClickDragLogic, CellClicked, Hover, NoSelection, ReleaseSelection, RowColumnClicked
}

object AdjMatrixClickDragLogicTests extends TestSuite {
	def tests = Tests {
		val logic = AdjMatrixClickDragLogic
		test("mouseUp") {
			Seq(NoSelection, Hover(Cell(0, 0)), ReleaseSelection(Set(Cell(0, 0)), true)).foreach { state =>
				val result = logic.mouseUp(state, Cell(1, 0), 5)
				assert(result == Hover(Cell(1, 0))) // on mouseUp from these states, we just go to Hover state
			}

			// Release when only a single cell is in selection
			// isAdd=true
			val cell = Cell(1, 2)
			val clickedState = CellClicked(cell, cell, isAdd = true)
			// TODO consider what happens if you mouse up on a different cell than you dragged to? I think this could happen if the mouse
			// moves fast enough such that the mouseUp event happens before the mouseMove event triggers
			val result1 = logic.mouseUp(clickedState, Cell(1, 2), 5)
			assert(result1 == ReleaseSelection(Set(Cell(1, 2)), isAdd = true))
			// isAdd=false
			val clickedState2 = CellClicked(cell, cell, isAdd = false)
			val result2 = logic.mouseUp(clickedState2, cell, 5)
			assert(result2 == ReleaseSelection(Set(cell), isAdd = false))
		}
		test("mouseup - Releasing when selection is two cells side-by (horizontally)") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X X . .
			// 2 . . . . .
			// 3 . . . . .

			val dragState = CellClicked(Cell(1, 1), Cell(2, 1), isAdd = true)
			val result = logic.mouseUp(dragState, Cell(2, 1), 5)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(2, 1)), isAdd = true))
		}
		test ("mouseup - when selection is three cells vertically") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . X . . .
			// 3 . X . . .

			val dragState = CellClicked(Cell(1, 1), Cell(1, 3), isAdd = false)
			val result = logic.mouseUp(dragState, Cell(1, 3), 5)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(1, 2), Cell(1, 3)), isAdd = false))
		}
		test ("mouseup - when selection is a single cell") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . . . .
			// 3 . . . . .

			val dragState = CellClicked(Cell(1, 1), Cell(1, 1), isAdd = true)
			val result = logic.mouseUp(dragState, Cell(1, 1), 5)
			assert(result == ReleaseSelection(Set(Cell(1, 1)), isAdd = true))
		}
		test ("mouseup - when selection is diagonal one square (it defaults to horizontal)") {
			// ASCII art of the selection:
			//   0 1 2 3 4
			// 0 . . . . .
			// 1 . X . . .
			// 2 . . X . .
			// 3 . . . . .

			val dragState = CellClicked(Cell(1, 1), Cell(2, 2), isAdd = false)
			val result = logic.mouseUp(dragState, Cell(2, 2), 5)
			assert(result == ReleaseSelection(Set(Cell(1, 1), Cell(1, 2)), isAdd = false))
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

			val dragState = CellClicked(Cell(1, 1), Cell(3, 2), isAdd = false)
			val result = logic.mouseUp(dragState, Cell(3, 2), 5)
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

			val dragState = CellClicked(Cell(0, 0), Cell(4,4), isAdd = false)
			val result = logic.mouseUp(dragState, Cell(4,4), 5)
			assert(result == ReleaseSelection(Set(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3), Cell(0, 4)), isAdd = false))
		}
		test("hovering over cells within bounds from a NoSelection state") {
			val result = logic.mouseMove(Cell(2, 3), NoSelection, 5)
			assert(result == Hover(Cell(2, 3)))
		}
		test("hovering over the row area") {
			val result = logic.mouseMove(Row(2), NoSelection, 5)
			assert(result == Hover(Row(2)))
			// previous state was Hovering over a cell:
			val result2 = logic.mouseMove(Row(2), Hover(Cell(1, 1)), 5)
			assert(result2 == Hover(Row(2)))
			// previous state was clicking on cells (so we don't change the state at all):
			val clickedState = CellClicked(Cell(0,0), Cell(0,1), isAdd = true)
			val result3 = logic.mouseMove(Row(2), clickedState, 5)
			assert(result3 == clickedState)
			// previous state was clicking on a row (so we don't change the state at all):
			val rccState = RowColumnClicked(Row(1), isAdd = false)
			val result4 = logic.mouseMove(Row(2), rccState, 5)
			assert(result4 == rccState)
		}
		test("hovering over cells within bounds from a Hover state") {
			val result = logic.mouseMove(Cell(1, 4), Hover(Cell(2, 3)), 5)
			assert(result == Hover(Cell(1, 4)))
		}

		test("end-to-end handleEvent: NoSelection now Hover") {
			val initialState = NoSelection
			val nodeCount = 5
			val newState = logic.handleEvent(
				AdjMatrixMouseMove(120, 80), // don't matter for this function
				initialState,
				nodeCount,
				Cell(0, 1),
				Set.empty // doesn't matter for this test
			)
			assert(newState == Hover(Cell(0, 1)))
		}
		test("end-to-end handleEvent: Hover now clicked") {
			val initialCell = Cell(2, 3)
			val initialState = Hover(initialCell)
			val nodeCount = 5

			// test with no existing edge
			val filledInCells = Set.empty[Cell]
			val newState = logic.handleEvent(
				AdjMatrixMouseDown(320, 220),
				initialState,
				nodeCount,
				initialCell,
				filledInCells
			)
			// since no edge exists, isAdd should be true (we are preparing to add the edge)
			assert(newState == CellClicked(initialCell, initialCell, isAdd = true))

			// test with an existing edge
			val filledInCells2 = Set(initialCell)
			val newState2 = logic.handleEvent(
				AdjMatrixMouseDown(320, 220),
				initialState,
				nodeCount,
				initialCell,
				filledInCells2
			)
			// since edge exists, isAdd should be false (we are preparing to remove the edge)
			assert(newState2 == CellClicked(initialCell, initialCell, isAdd = false))
		}
	}
}