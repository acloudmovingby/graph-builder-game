package graphcontroller.model.adjacencymatrix

import scala.collection.immutable.ListSet

import graphi.MapGraph
import graphcontroller.controller.{
	AdjacencyMatrixEvent, AdjMatrixMouseDown, AdjMatrixMouseLeave, AdjMatrixMouseUp, AdjMatrixMouseMove
}
import graphcontroller.dataobject.{AdjMatrixZone, Cell}
import graphcontroller.model.adjacencymatrix.{
	AdjMatrixInteractionState, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection
}

// TODO: I hate this name
object AdjMatrixClickDragLogic {
	val padding = 10 // pixels of padding around the adjacency matrix

	def handleEvent(
		event: AdjacencyMatrixEvent,
		currentState: AdjMatrixInteractionState,
		nodeCount: Int,
		zone: AdjMatrixZone,
		filledInCells: Set[Cell]
	): AdjMatrixInteractionState = {
		zone match {
			case cell: Cell =>
				if (nodeCount == 0 || nodeCount == 1) {
					// with 0 or 1 node, no edges are possible so no interaction
					NoSelection
				} else {
					event match {
						case AdjMatrixMouseUp(_, _) =>
							mouseUp(currentState, cell)
						case AdjMatrixMouseMove(_, _) =>
							mouseMove(cell, currentState, nodeCount)
						case AdjMatrixMouseLeave(_, _) => mouseLeave(currentState)
						case AdjMatrixMouseDown(mouseX, mouseY) =>
							mouseDown(currentState, nodeCount, filledInCells, cell)
					}
				}
			case _ => NoSelection // TODO implement other cases
		}

	}

	def mouseUp(
		currentState: AdjMatrixInteractionState,
		hoveredCell: Cell
	): AdjMatrixInteractionState = {
		currentState match {
			case NoSelection | ReleaseSelection(_, _) | Hover(_) =>
				// This situation can happen when someone clicks down outside the matrix then moves the mouse inside, then releases
				Hover(hoveredCell) // just go to hover state
			case Clicked(cell, isAdd) =>
				ReleaseSelection(Set(cell), isAdd) // selection is 1 cell
			case d: DragSelecting =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
		}
	}

	def mouseDown(
		currentState: AdjMatrixInteractionState,
		nodeCount: Int,
		filledInCells: Set[Cell],
		clickedCell: Cell
	): AdjMatrixInteractionState = {
		// TODO get rid of this logging once we're confident it's working
		val isAdd = !filledInCells.contains(clickedCell)
		Clicked(clickedCell, isAdd = isAdd)
	}

	def mouseMove(
		cell: Cell,
		currentState: AdjMatrixInteractionState,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		if (cell.col < 0 || cell.col >= nodeCount || cell.row < 0 || cell.row >= nodeCount) {
			// out of bounds (I think it's handled higher up in logic as well, but doesn't hurt to double check)
			NoSelection
		} else {
			currentState match {
				case NoSelection =>
					Hover(cell) // hovering over cell
				case Hover(_) =>
					Hover(cell) // update hover position
				case Clicked(startCell, isAdd) =>
					// start drag selection
					DragSelecting(startCell, cell, isAdd)
				case d: DragSelecting =>
					// update drag selection
					d.copy(currentHoveredCell = cell)
				case r: ReleaseSelection =>
					// do nothing, selection already made
					Hover(cell)
			}
		}
	}

	def mouseLeave(
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = {
		// actually, let's make it so if it leaves while you were selected, it then release tha selection
		// (whether it was just Clicked or DragSelecting)
		currentState match {
			case NoSelection | Hover(_) =>
				NoSelection // nothing selected, so just go to no selection
			case Clicked(cell, isAdd) =>
				ReleaseSelection(Set(cell), isAdd) // finalize selection
			case d: DragSelecting =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
			case r: ReleaseSelection =>
				// already released selection, so just go to no selection
				NoSelection
		}
	}
}
