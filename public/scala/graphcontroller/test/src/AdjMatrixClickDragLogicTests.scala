import scala.collection.immutable.ListSet
import utest.*

import graphcontroller.{AdjMatrixClickDragLogic, AdjMatrixSelectionState, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection}

object AdjMatrixClickDragLogicTests extends TestSuite {
	def tests = Tests {
		test("mouseUp") {
			val logic = new AdjMatrixClickDragLogic()
			// these three states will result in thrown exception since they should never happen (you can't release a 
			// mouse click if you never clicked in the first place)
			Seq(NoSelection, Hover((0, 0)), ReleaseSelection(ListSet((0,0)), true)).foreach { state =>
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
	}
}
