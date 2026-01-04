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
	cells: ListSet[(Int, Int)], // set of cells currently selected in drag, use ListSet to maintain order of selection
	isHorizontal: Boolean, // true = horizontal drag, false = vertical drag
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixSelectionState
/** When we release the selection and actually want to apply its addition/removal. State
 * is essentially treated like NoSelection apart from that (since now the selection has been released) */
case class ReleaseSelection(
	cells: ListSet[(Int, Int)], // set of cells selected on release
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
				ReleaseSelection(ListSet(cell), isAdd) // selection is 1 cell
			case DragSelecting(cells, isHorizontal, isAdd) =>
				ReleaseSelection(cells, isAdd) // finalize selection
		}
	}

	def mouseMove(
		currentState: AdjMatrixSelectionState,
		hoveredCell: (Int, Int)
	): AdjMatrixSelectionState = {
		currentState match {
			case NoSelection =>
				Hover(hoveredCell)
			case Hover(_) =>
				Hover(hoveredCell)
			case Clicked(startCell, isAdd) =>
				// determine if horizontal or vertical drag
				val isHorizontal = startCell._1 == hoveredCell._1
				DragSelecting(ListSet(startCell, hoveredCell), isHorizontal, isAdd)
			case DragSelecting(cells, isHorizontal, isAdd) =>
				// extend selection
				val lastCell = cells.last
				val newCells = if (isHorizontal) {
					// horizontal drag, fix row index
					val row = lastCell._1
					val colRange = if (hoveredCell._2 >= lastCell._2) {
						(lastCell._2 + 1) to hoveredCell._2
					} else {
						hoveredCell._2 to (lastCell._2 - 1)
					}
					colRange.map(col => (row, col))
				} else {
					// vertical drag, fix column index
					val col = lastCell._2
					val rowRange = if (hoveredCell._1 >= lastCell._1) {
						(lastCell._1 + 1) to hoveredCell._1
					} else {
						hoveredCell._1 to (lastCell._1 - 1)
					}
					rowRange.map(row => (row, col))
				}
				DragSelecting(cells ++ newCells, isHorizontal, isAdd)
			case ReleaseSelection(_, _) =>
				// once released, treat as no selection and start hover anew
				Hover(hoveredCell)
		}
	}

}
