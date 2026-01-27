package graphcontroller.model

import graphcontroller.controller.{AdjMatrixMouseDown, AdjacencyMatrixEvent, Event, ExportCopy, Initialization, NoOp}
import graphcontroller.dataobject.AdjMatrixDimensions
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionLogic, ReleaseSelection}
import graphcontroller.shared.AdjMatrixCoordinateConverter
import AdjMatrixCoordinateConverter.convertCoordinatesToZone

/** Pure function that takes current state and the input event and then calculates the new state */
object Model {
	def handleEvent(event: Event, state: State): State = {
		val newState = event match {
			case e: Initialization => handleInitializationEvent(e, state)
			case e: AdjacencyMatrixEvent => handleAdjacencyMatrixEvent(e, state)
			case ExportCopy => state.copy(exportedDot = Some(state.graph.toDot))
			case NoOp => state
		}
		newState
	}

	private def handleInitializationEvent(event: Initialization, state: State): State = {
		println("initializing model with adj matrix dimensions: " + event.adjMatrixWidth + "x" + event.adjMatrixHeight)
		state.copy(adjMatrixDimensions = AdjMatrixDimensions(event.adjMatrixWidth, event.adjMatrixHeight, event.padding, event.numberPadding))
	}

	private def handleAdjacencyMatrixEvent(event: AdjacencyMatrixEvent, state: State): State = {
		val zone = convertCoordinatesToZone(
			event.mouseX,
			event.mouseY,
			state.adjMatrixDimensions,
			state.graph.nodeCount
		)

		// calculate change in adjacency matrix state
		val newAdjMatrixState = AdjMatrixInteractionLogic.handleMouseEvent(
			event,
			state.adjMatrixState,
			state.graph.nodeCount,
			zone,
			state.filledInCells
		)

		newAdjMatrixState match {
			// if the state changed to a ReleaseSelection, the selection was released and therefore we need to update the graph accordingly
			case ReleaseSelection(cells, isAdd) =>
				val newGraph = cells.foldLeft(state.graph) { (graph, cell) =>
					if (cell.row != cell.col) { // Currently we don't support self-loops
						if (isAdd) {
							graph.addEdge(cell.row, cell.col)
						} else {
							graph.removeEdge(cell.row, cell.col)
						}
					} else graph
				}

				state.copy(
					graph = newGraph,
					adjMatrixState = newAdjMatrixState
				)
			case _ => state.copy(adjMatrixState = newAdjMatrixState)
		}
	}
}