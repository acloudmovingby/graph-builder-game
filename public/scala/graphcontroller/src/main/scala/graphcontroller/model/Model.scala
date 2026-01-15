package graphcontroller.model

import graphcontroller.controller.{AdjMatrixMouseDown, AdjacencyMatrixEvent, Event, Initialization, NoOp}
import graphcontroller.model.adjacencymatrix.AdjMatrixClickDragLogic

/** Pure function that takes current state and the input event and then calculates the new state */
object Model {
    def handleEvent(event: Event, state: State): State = {
        val newState = event match {
            case e: Initialization => handleInitializationEvent(e, state)
            case e: AdjacencyMatrixEvent => handleAdjacencyMatrixEvent(e, state)
            case NoOp => state
        }
        newState
    }

    private def handleInitializationEvent(event: Initialization, state: State): State = {
        println("initializing model with adj matrix dimensions: " + event.adjMatrixWidth + "x" + event.adjMatrixHeight)
        state.copy(adjMatrixDimensions = (event.adjMatrixWidth, event.adjMatrixHeight))
    }

    private def handleAdjacencyMatrixEvent(event: AdjacencyMatrixEvent, state: State): State = {
        val newAdjMatrixState = AdjMatrixClickDragLogic.handleEvent(event, state.adjMatrixState)
        state.copy(adjMatrixState = newAdjMatrixState)
    }
}