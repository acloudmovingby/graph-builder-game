package graphcontroller.components.adjacencymatrix

import graphcontroller.controller.*
import graphcontroller.dataobject.*
import graphcontroller.components.adjacencymatrix.*
import graphi.MapGraph

import scala.collection.immutable.ListSet

/**
 * Code to calculate changes in the Adjacency Matrix interaction state based on input events.
 */
object AdjMatrixInteractionLogic {
	/**
	 * Given a mouse event in the adjacency matrix area, determine the new interaction state.
	 *
	 * @param event The mouse event that occurred.
	 * @param currentState The current interaction state before the event.
	 * @param nodeCount The number of nodes in the graph (to determine valid cells).
	 * @param zone The zone in the adjacency matrix where the event occurred (translated previously from mouse coordinates).
	 * @param filledInCells The set of currently filled-in cells in the adjacency matrix (i.e. where edges exist).
	 * @return The new interaction state after processing the event.
	 */
	def handleMouseEvent(
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

	/** When someone releases the mouse click */
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
				ReleaseSelection(d.selectedCells, d.isAdd)
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
