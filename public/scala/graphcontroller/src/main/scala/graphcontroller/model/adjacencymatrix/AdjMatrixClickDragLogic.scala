package graphcontroller.model.adjacencymatrix

import scala.collection.immutable.ListSet

import graphi.MapGraph
import graphcontroller.controller.{
	AdjacencyMatrixEvent, AdjMatrixMouseDown, AdjMatrixMouseLeave, AdjMatrixMouseUp, AdjMatrixMouseMove
}
import graphcontroller.model.adjacencymatrix.{
	AdjMatrixInteractionState, Clicked, DragSelecting, Hover, NoSelection, ReleaseSelection
}

// TODO: I hate this name
object AdjMatrixClickDragLogic {
	def handleEvent(
		event: AdjacencyMatrixEvent,
		currentState: AdjMatrixInteractionState,
		adjMatrixDimensions: (Int, Int),
		nodeCount: Int,
		filledInCells: Set[(Int, Int)]
	): AdjMatrixInteractionState = {
		if (nodeCount == 0 || nodeCount == 1) {
			// with 0 or 1 node, no edges are possible so no interaction
			NoSelection
		} else {
			event match {
				case AdjMatrixMouseUp => mouseUp(currentState)
				case AdjMatrixMouseMove(mouseX, mouseY) =>
					val (row, col) = convertMouseCoordinatesToCell(mouseX, mouseY, adjMatrixDimensions, nodeCount)
					mouseMove(row, col, currentState, nodeCount)
				case AdjMatrixMouseLeave => mouseLeave(currentState)
				case AdjMatrixMouseDown =>
					mouseDown(currentState, nodeCount, filledInCells)
			}
		}
	}
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
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = {
		currentState match {
			case NoSelection | ReleaseSelection(_, _) | Hover(_) =>
				// TODO I think this is actually possible and we should think about what to do here
				// (you can mousedown outside the matrix, then move the cursor onto the matrix, then mouseup)
				throw new Exception("Invalid state: mouseUp called but mousedown was never called")
			case Clicked(cell, isAdd) =>
				ReleaseSelection(Set(cell), isAdd) // selection is 1 cell
			case d: DragSelecting =>
				ReleaseSelection(d.selectedCells, d.isAdd) // finalize selection
		}
	}

	def mouseDown(
		currentState: AdjMatrixInteractionState,
		nodeCount: Int,
		filledInCells: Set[(Int, Int)],
	): AdjMatrixInteractionState = {
		currentState match {
			case Hover((row, col)) =>
				// Note how we reverse row/col here because graph edges are (from, to) = (row, col)
				// I found it more intuitive to have the matrix with rows as "from" and columns as "to"
				// that way you can drag horizontally to add/remove edges from a single node to multiple nodes,
				// or drag vertically to add/remove edges to a single node from multiple nodes
				val isAdd = if (filledInCells.contains((col, row))) {
					println("Edge EXISTS, so preparing to REMOVE it")
					false
				} else {
					println("Edge does NOT exist, so preparing to ADD it")
					true
				}
				Clicked((row, col), isAdd = isAdd)
			case _ =>
				NoSelection
		}
	}

	def mouseMove(
		row: Int,
		col: Int,
		currentState: AdjMatrixInteractionState,
		nodeCount: Int
	): AdjMatrixInteractionState = {
		if (col < 0 || col >= nodeCount || row < 0 || row >= nodeCount) {
			// out of bounds (I think it's handled higher up in logic as well, but doesn't hurt to double check)
			NoSelection
		} else {
			currentState match {
				case NoSelection =>
					Hover((row, col)) // hovering over cell
				case Hover(_) =>
					Hover((row, col)) // update hover position
				case Clicked(startCell, isAdd) =>
					// start drag selection
					DragSelecting(startCell, (row, col), isAdd)
				case d: DragSelecting =>
					// update drag selection
					d.copy(currentHoveredCell = (row, col))
				case r: ReleaseSelection =>
					// do nothing, selection already made
					NoSelection
			}
		}
	}

	def mouseLeave(
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = NoSelection

	def convertMouseCoordinatesToCell(
		mouseX: Int,
		mouseY: Int,
		adjMatrixDimensions: (Int, Int),
		nodeCount: Int
	): (Int, Int) = {
		if (nodeCount == 0) throw new Exception("No nodes in graph, cannot convert mouse coords to cell")
		val cellWidth = adjMatrixDimensions._1 / nodeCount
		val cellHeight = adjMatrixDimensions._2 / nodeCount
		val col = mouseX / cellWidth
		val row = mouseY / cellHeight
		(row, col)
	}
}
