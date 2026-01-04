import scala.collection.immutable.ListSet
import utest.*

import graphcontroller.{AdjMatrixClickDragLogic, AdjMatrixSelectionState, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection}

object AdjMatrixClickDragLogicTests extends TestSuite {
	def tests = Tests {
		test("mouseUp") {
			val logic = new AdjMatrixClickDragLogic()
			// these three states will result in thrown exception since they should never happen (you can't release a
			// mouse click if you never clicked in the first place)
			Seq(NoSelection, Hover((0, 0)), ReleaseSelection(ListSet((0, 0)), true)).foreach { state =>
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
			assert(result1 == ReleaseSelection(ListSet((1, 2)), isAdd = true))
			// isAdd=false
			val clickedState2 = Clicked((3, 4), isAdd = false)
			val result2 = logic.mouseUp(clickedState2)
			assert(result2 == ReleaseSelection(ListSet((3, 4)), isAdd = false))

			// DragSelecting state:
			// isHorizontal=true, isAdd=true
			val dragState = DragSelecting(ListSet((1, 2), (1, 3), (1, 4)), isHorizontal = true, isAdd = true)
			val result3 = logic.mouseUp(dragState)
			assert(result3 == ReleaseSelection(ListSet((1, 2), (1, 3), (1, 4)), isAdd = true))
			// isHorizontal=false, isAdd=false
			val dragState2 = DragSelecting(ListSet((2, 1), (3, 1), (4, 1)), isHorizontal = false, isAdd = false)
			val result4 = logic.mouseUp(dragState2)
			assert(result4 == ReleaseSelection(ListSet((2, 1), (3, 1), (4, 1)), isAdd = false))
		}
		test("mouseMove") {
			val logic = new AdjMatrixClickDragLogic()

			// NoSelection state
			val noSelState = NoSelection
			val hoverCell1 = (2, 3)
			val result1 = logic.mouseMove(noSelState, hoverCell1)
			assert(result1 == Hover(hoverCell1))

			// Hover state
			val hoverState = Hover((1, 1))
			val hoverCell2 = (4, 5)
			val result2 = logic.mouseMove(hoverState, hoverCell2)
			assert(result2 == Hover(hoverCell2))

			// Clicked state
			val clickedState = Clicked((2, 2), isAdd = true)
			val hoverCell3 = (2, 5) // horizontal drag
			val result3 = logic.mouseMove(clickedState, hoverCell3)
			assert(result3 == DragSelecting(ListSet((2, 2), (2, 5)), isHorizontal = true, isAdd = true))

			val hoverCell4 = (5, 2) // vertical drag
			val result4 = logic.mouseMove(clickedState, hoverCell4)
			assert(result4 == DragSelecting(ListSet((2, 2), (5, 2)), isHorizontal = false, isAdd = true))

			// DragSelecting state
			val dragState = DragSelecting(ListSet((1, 1), (1, 3)), isHorizontal = true, isAdd = false)
			val hoverCell5 = (1, 5)
			val result5 = logic.mouseMove(dragState, hoverCell5)
			assert(result5 == DragSelecting(ListSet((1, 1), (1, 3), (1, 4), (1, 5)), isHorizontal = true, isAdd = false))

			val dragState2 = DragSelecting(ListSet((2, 2), (4, 2)), isHorizontal = false, isAdd = true)
			val hoverCell6 = (6, 2)
			val result6 = logic.mouseMove(dragState2, hoverCell6)
			assert(result6 == DragSelecting(ListSet((2, 2), (4, 2), (5, 2), (6, 2)), isHorizontal = false, isAdd = true))

			// TODO:
			// - add diagonal movement test for Clicked state, but need to implement (default to horizontal? choose based on larger delta?)
			// - add test for changing direction mid-drag
			// - break up these tests into smaller tests

			// Add test for diagonal movement with DragSelecting (can happen in normal use if person moves mouse fast enough)
			// If horizontal, should extend horizontally and ignore the vertical change, and vice versa.
			val dragState3 = DragSelecting(ListSet((3, 3), (3, 4)), isHorizontal = true, isAdd = true)
			val hoverCellDiagonal = (5, 6) // diagonal movement
			val resultDiagonal = logic.mouseMove(dragState3, hoverCellDiagonal)
			// should extend horizontally only to (3,6)
			assert(resultDiagonal == DragSelecting(ListSet((3, 3), (3, 4), (3, 5), (3, 6)), isHorizontal = true, isAdd = true))

			// diagonal movement with horizontal=false
			val dragState4 = DragSelecting(ListSet((3, 3), (5, 3)), isHorizontal = false, isAdd = false)
			val hoverCellDiagonal2 = (6, 5) // diagonal movement
			val resultDiagonal2 = logic.mouseMove(dragState4, hoverCellDiagonal2)
			// should extend vertically only to (6,3)
			assert(resultDiagonal2 == DragSelecting(ListSet((3, 3), (5, 3), (6, 3)), isHorizontal = false, isAdd = false))

			// ReleaseSelection state
			val releaseState = ReleaseSelection(ListSet((3, 3), (3, 4)), isAdd = true)
			val hoverCell7 = (5, 5)
			val result7 = logic.mouseMove(releaseState, hoverCell7)
			assert(result7 == Hover(hoverCell7))
		}
	}
}