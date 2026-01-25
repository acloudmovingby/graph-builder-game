package graphcontroller.model.adjacencymatrix

import graphcontroller.dataobject.{AdjMatrixZone, Cell}

sealed trait AdjMatrixInteractionState

/** Mouse is currently not hovering over adjacency matrix at all */
case object NoSelection extends AdjMatrixInteractionState
/** Mouse is hovering over a cell but not clicked/pressed. */
case class Hover(zone: AdjMatrixZone) extends AdjMatrixInteractionState
/** Mouse is currently dragging to select/deselect cells */
case class Clicked(
	startCell: Cell,
	currentHoveredCell: Cell,
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixInteractionState {
	def selectedCells: Set[Cell] = {
		if (Math.abs(currentHoveredCell.row - startCell.row) <= Math.abs(currentHoveredCell.col - startCell.col)) {
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

/**
 * When we release the selection and actually want to apply its addition/removal.
 * */
case class ReleaseSelection(
	cells: Set[Cell], // set of cells selected on release
	isAdd: Boolean // true = adding selection to graph, false = removing selection
) extends AdjMatrixInteractionState