package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.NoSelection
import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
    // TODO put this in a config file somewhere?
    private val edgePresentColor = "black"
    private val hoverEdgePresentColor = "#F2813B"
    private val hoverNoEdgeColor = "#E2E2E2"


    private def gridLines(nodeCount: Int, adjMatrixWidth: Int, adjMatrixHeight: Int): Seq[CanvasLine] = {
        // check this first to avoid division by zero
        if (nodeCount == 0) Seq.empty else {
            val width = adjMatrixWidth / nodeCount
            val height = adjMatrixHeight / nodeCount

            for {
                i <- 1 until nodeCount
                verticalLine = CanvasLine(
                    from = Point(x = width * i, y = 0),
                    to = Point(x = width * i, y = adjMatrixHeight),
                    width = 1,
                    color = "lightgray"
                )
                horizontalLine = CanvasLine(
                    from = Point(x = 0, y = height * i),
                    to = Point(x = adjMatrixWidth, y = height * i),
                    width = 1,
                    color = "lightgray"
                )
                lines <- Seq(verticalLine, horizontalLine)
            } yield lines
        }
    }

    /** Render data for matrix cells representing existing edges */
    private def filledInCells(
        nodeCount: Int,
        edges: Set[(Int, Int)],
        adjMatrixWidth: Int,
        adjMatrixHeight: Int
    ) = if (nodeCount == 0) Seq.empty else {
        val cellWidth = adjMatrixWidth / nodeCount
        val cellHeight = adjMatrixHeight / nodeCount
        edges.toSeq.map { case (from, to) =>
            RectangleCanvas(
                x = to * cellWidth,
                y = from * cellHeight,
                width = cellWidth,
                height = cellHeight,
                color = edgePresentColor
            )
        }
    }

    def render(state: State): AdjacencyMatrixViewData = {
        val nodeCount = state.graph.nodeCount

        // Only show grid lines when hovering over the canvas area (because it's cool and less distracting when you add nodes)
        val _gridLines = if (state.adjMatrixState == NoSelection) {
            Seq.empty
        } else {
            gridLines(nodeCount, state.adjMatrixDimensions._1, state.adjMatrixDimensions._2)
        }

        val cells = filledInCells(
            nodeCount,
            state.graph.getEdges,
            state.adjMatrixDimensions._1,
            state.adjMatrixDimensions._2
        )

        // put gridlines after cells so they get drawn on top
        AdjacencyMatrixViewData(cells ++ _gridLines)
    }
}