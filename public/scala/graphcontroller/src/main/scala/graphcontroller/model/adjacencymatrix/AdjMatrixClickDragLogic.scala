package graphcontroller.model.adjacencymatrix

import scala.collection.immutable.ListSet

import graphi.MapGraph
import graphcontroller.controller.{
	AdjacencyMatrixEvent, AdjMatrixMouseDown, AdjMatrixMouseLeave, AdjMatrixMouseUp, AdjMatrixMouseMove
}
import graphcontroller.dataobject.{AdjMatrixZone, Cell, Column, Corner, NoCell, Row}
import graphcontroller.model.adjacencymatrix.{
	AdjMatrixInteractionState, CellClicked, Hover, NoSelection, ReleaseSelection
}

// TODO: I hate this name
object AdjMatrixClickDragLogic {

	def handleEvent(
		event: AdjacencyMatrixEvent,
		currentState: AdjMatrixInteractionState,
		nodeCount: Int,
		zone: AdjMatrixZone,
		filledInCells: Set[Cell]
	): AdjMatrixInteractionState = {
		if (nodeCount == 0 || nodeCount == 1) {
			// with 0 or 1 node, no edges are possible so no interaction
			NoSelection
		} else {
			event match {
				case AdjMatrixMouseUp(_, _) =>
					mouseUp(currentState, zone, nodeCount)
				case AdjMatrixMouseMove(_, _) =>
					mouseMove(zone, currentState, nodeCount)
				case AdjMatrixMouseLeave(_, _) =>
					mouseLeave(currentState, nodeCount)
				case AdjMatrixMouseDown(mouseX, mouseY) =>
					mouseDown(filledInCells, zone, nodeCount)
			}
		}
	}

	def mouseUp(
		currentState: AdjMatrixInteractionState,
		hoveredZone: AdjMatrixZone,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		currentState match {
			case NoSelection | ReleaseSelection(_, _) | Hover(_) =>
				// This situation can happen when someone clicks down outside the matrix then moves the mouse inside, then releases
				Hover(hoveredZone) // just go to hover state
			case d: CellClicked =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
			case rcc: RowColumnClicked =>
				ReleaseSelection(rcc.selectedCells(nodeCount), rcc.isAdd)
		}
	}

	def mouseDown(
		filledInCells: Set[Cell],
		zone: AdjMatrixZone,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		zone match {
			case clickedCell: Cell =>
				val isAdd = !filledInCells.contains(clickedCell)
				CellClicked(clickedCell, clickedCell, isAdd = isAdd)
			case rc: (Row | Column) =>
				val cells = rc match {
					case r: Row => r.cells(nodeCount, excludeSelfEdges = true)
					case c: Column => c.cells(nodeCount, excludeSelfEdges = true)
				}
				// determine if we're adding: if all cells in that row are filled in, then we're removing, else adding
				val isAdd = !cells.forall(c => filledInCells.contains(c))
				RowColumnClicked(rc, isAdd = isAdd)
			case Corner | NoCell => NoSelection
		}
	}

	def mouseMove(
		zone: AdjMatrixZone,
		currentState: AdjMatrixInteractionState,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		(currentState, zone) match {
			case (NoSelection, _) =>
				Hover(zone) // hovering over cell
			case (Hover(_), _) =>
				Hover(zone) // update hover position
			case (clickedState: CellClicked, cell: Cell) =>
				// update drag selection
				clickedState.copy(currentHoveredCell = cell)
			case (clickedState: CellClicked, _) =>
				clickedState // ignore moves outside cells while dragging
			case (rcc: RowColumnClicked, _) =>
				rcc // currently not updating the selection when moving to a new row/column/cell
			case (r: ReleaseSelection, _) =>
				// do nothing, selection already made
				Hover(zone)
		}
	}

	def mouseLeave(
		currentState: AdjMatrixInteractionState,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		// actually, let's make it so if it leaves while you were selected, it then releases tha selection
		currentState match {
			case NoSelection | Hover(_) =>
				NoSelection // nothing selected, so just go to no selection
			case d: CellClicked =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
			case rcc: RowColumnClicked =>
				ReleaseSelection(rcc.selectedCells(nodeCount), rcc.isAdd) // finalize selection
			case r: ReleaseSelection =>
				// already released selection, so just go to no selection
				NoSelection
		}
	}
}
