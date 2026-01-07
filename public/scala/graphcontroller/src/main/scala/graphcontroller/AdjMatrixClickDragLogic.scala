package graphcontroller

import scala.collection.immutable.ListSet

sealed trait AdjMatrixSelectionState

/** Mouse is currently not hovering over adjacency matrix at all */
case object NoSelection extends AdjMatrixSelectionState
/** Mouse is hovering over a cell but not clicked/pressed */
case class Hover(edge: (Int, Int)) extends AdjMatrixSelectionState
/** Mouse is clicked/pressed on a cell but not yet moved. Caller is responsible for deciding if this will add an edge or remove one */
case class Clicked(edge: (Int, Int), isAdd: Boolean) extends AdjMatrixSelectionState
/** Mouse is currently dragging to select/deselect cells */
case class DragSelecting(
	startCell: (Int, Int),
	currentHoveredCell: (Int, Int),
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixSelectionState {
	def selectedCells: Set[(Int, Int)] = {
		if (startCell._1 == currentHoveredCell._1) {
			// horizontal drag
			val row = startCell._1
			val colRange = if (currentHoveredCell._2 >= startCell._2) {
				startCell._2 to currentHoveredCell._2
			} else {
				currentHoveredCell._2 to startCell._2
			}
			colRange.map(col => (row, col)).toSet
		} else {
			// vertical drag
			val col = startCell._2
			val rowRange = if (currentHoveredCell._1 >= startCell._1) {
				startCell._1 to currentHoveredCell._1
			} else {
				currentHoveredCell._1 to startCell._1
			}
			rowRange.map(row => (row, col)).toSet
		}
	}
}
/** When we release the selection and actually want to apply its addition/removal. State
 * is essentially treated like NoSelection apart from that (since now the selection has been released) */
case class ReleaseSelection(
	cells: Set[(Int, Int)], // set of cells selected on release
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixSelectionState

class AdjMatrixClickDragLogic {
	/* events that can happen:
	- mousedown on cell
		- start selection process
	- mousemove to another cell
		- extend selection process or ignore if not in drag mode
	- mousemove off the matrix
		- end selection process or ignore if not in drag mode
	- mouseup on cell
		- end selection process, or ignore if not in drag mode
	- mouseup off the matrix
		- end selection process, or ignore if not in drag mode
	- mousemove back onto the matrix
	- mousemove to cell not in the existing horiz/vert drag path (maybe we're actually okay with this)
	*/

	/*
	How to change state of application, options:
	- use mutable state within this class to track drag state. Graph rendering queries mutable state to determine what to render
	- use immutable state, and return new state on each event
	- use callbacks to inform outside code of state changes
	- combination of the above

	Option 1: No internal mutable state
	- on each event pass in existing state, return new state
	- these functions are pure functions
	- easier to test
	- outside code responsible for storing state
	- outside code responsible for informing rest of app of state changes

	Option 2: Internal mutable state and power to change graph state
	- this class responsible for storing state
	- this class responsible for doing permanent state changes by directly modifying graph controller or underlying graph model

	 */

	def mouseUp(
		currentState: AdjMatrixSelectionState
	): AdjMatrixSelectionState = {
		currentState match {
			case NoSelection | Hover(_) | ReleaseSelection(_, _) =>
				throw new Exception("Invalid state: mouseUp called but mousedown was never called")
			case Clicked(cell, isAdd) =>
				ReleaseSelection(Set(cell), isAdd) // selection is 1 cell
			case d: DragSelecting =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
		}
	}
}
