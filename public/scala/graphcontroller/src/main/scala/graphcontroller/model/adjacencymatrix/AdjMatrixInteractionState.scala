package graphcontroller.model.adjacencymatrix

import graphcontroller.model.adjacencymatrix.Cell

sealed trait AdjMatrixInteractionState

/** Mouse is currently not hovering over adjacency matrix at all */
case object NoSelection extends AdjMatrixInteractionState
/** Mouse is hovering over a cell but not clicked/pressed. */
case class Hover(cell: Cell) extends AdjMatrixInteractionState
/** Mouse is clicked/pressed on a cell but not yet moved. TBH, I could probably remove this and just
 * use DragSelecting but with startCell == currentHoveredCell. However, I had this idea of adding an
 * animation when you first click, so keeping it separate for now. */
case class Clicked(cell: Cell, isAdd: Boolean) extends AdjMatrixInteractionState
/** Mouse is currently dragging to select/deselect cells */
case class DragSelecting(
	startCell: Cell,
	currentHoveredCell: Cell,
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixInteractionState {
	def selectedCells: Set[Cell] = {
		if (startCell.row == currentHoveredCell.row) {
			// horizontal drag
			val row = startCell.row
			val colRange = if (currentHoveredCell.col >= startCell.col) {
				startCell.col to currentHoveredCell.col
			} else {
				currentHoveredCell.col to startCell.col
			}
			colRange.map(col => Cell(row, col)).toSet
		} else {
			// vertical drag
			val col = startCell.col
			val rowRange = if (currentHoveredCell.row >= startCell.row) {
				startCell.row to currentHoveredCell.row
			} else {
				currentHoveredCell.row to startCell.row
			}
			rowRange.map(row => Cell(row, col)).toSet
		}
	}
}
/** When we release the selection and actually want to apply its addition/removal. */
case class ReleaseSelection(
	cells: Set[Cell], // set of cells selected on release
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixInteractionState