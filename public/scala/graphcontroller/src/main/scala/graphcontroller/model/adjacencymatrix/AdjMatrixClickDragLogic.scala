package graphcontroller.model.adjacencymatrix

import scala.collection.immutable.ListSet
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
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = {
		event match {
			case AdjMatrixMouseUp => mouseUp(currentState)
			case AdjMatrixMouseMove(x, y) => mouseMove(x, y, currentState)
			case AdjMatrixMouseLeave => mouseLeave(currentState)
			case _ => currentState // other events not implemented yet
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

	def mouseMove(
		x: Int,
		y: Int,
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = {
		currentState match {
			case NoSelection =>
				Hover((x, y)) // hovering over cell
			case Hover(_) =>
				Hover((x, y)) // update hover position
			case Clicked(startCell, isAdd) =>
				// start drag selection
				DragSelecting(startCell, (x, y), isAdd)
			case d: DragSelecting =>
				// update drag selection
				d.copy(currentHoveredCell = (x, y))
			case r: ReleaseSelection =>
				// do nothing, selection already made
				NoSelection
		}
	}

	def mouseLeave(
		currentState: AdjMatrixInteractionState
	): AdjMatrixInteractionState = NoSelection
}
