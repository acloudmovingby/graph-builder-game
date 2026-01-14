package graphcontroller.model.adjacencymatrix

sealed trait AdjMatrixInteractionState

/** Mouse is currently not hovering over adjacency matrix at all */
case object NoSelection extends AdjMatrixInteractionState
/** Mouse is hovering over a cell but not clicked/pressed */
case class Hover(edge: (Int, Int)) extends AdjMatrixInteractionState
/** Mouse is clicked/pressed on a cell but not yet moved. */
case class Clicked(edge: (Int, Int), isAdd: Boolean) extends AdjMatrixInteractionState
/** Mouse is currently dragging to select/deselect cells */
case class DragSelecting(
	startCell: (Int, Int),
	currentHoveredCell: (Int, Int),
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixInteractionState {
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
/** When we release the selection and actually want to apply its addition/removal. */
case class ReleaseSelection(
	cells: Set[(Int, Int)], // set of cells selected on release
	isAdd: Boolean // true = adding selection, false = removing selection
) extends AdjMatrixInteractionState