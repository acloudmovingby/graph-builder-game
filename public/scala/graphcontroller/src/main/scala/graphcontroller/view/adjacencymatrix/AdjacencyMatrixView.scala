package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.{Cell, Hover, NoSelection}
import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	private val edgePresentColor = "black"
	private val hoverEdgePresentColor = "#F2813B"
	private val hoverNoEdgeColor = "#E2E2E2"


	private def calculateGridLines(state: State): Seq[CanvasLine] = {
		val nodeCount = state.graph.nodeCount
		val adjMatrixWidth = state.adjMatrixDimensions._1
		val adjMatrixHeight = state.adjMatrixDimensions._2
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

	private def hoveredCellHighlight(state: State, hoveredCell: Cell): Option[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount
		if (nodeCount == 0) None else {
			val cellWidth = state.adjMatrixDimensions._1 / nodeCount
			val cellHeight = state.adjMatrixDimensions._2 / nodeCount
			val color = if (state.graph.getEdges.contains(hoveredCell.toEdge)) hoverEdgePresentColor else hoverNoEdgeColor
			Some(RectangleCanvas(
				x = hoveredCell.col * cellWidth,
				y = hoveredCell.row * cellHeight,
				width = cellWidth,
				height = cellHeight,
				color = color
			))
		}
	}

	/** Render data for matrix cells representing existing edges */
	private def filledInCells(state: State): Seq[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val cellWidth = state.adjMatrixDimensions._1 / nodeCount
			val cellHeight = state.adjMatrixDimensions._2 / nodeCount
			state.graph.getEdges.toSeq.map { case (from, to) =>
				RectangleCanvas(
					x = to * cellWidth,
					y = from * cellHeight,
					width = cellWidth,
					height = cellHeight,
					color = edgePresentColor
				)
			}
		}
	}

	def render(state: State): AdjacencyMatrixViewData = {
		val shapes: Seq[RenderOp] = state.adjMatrixState match {
			case NoSelection => // fill in cells only, no grid lines
				val cells = filledInCells(state)
				cells
			case Hover(cell) => // fill in cells + hovered cell highlight + grid lines
				val cells = filledInCells(state)
				val gridLines = calculateGridLines(state)
				val hoveredCell = hoveredCellHighlight(state, cell)
				cells ++ hoveredCell.toSeq ++ gridLines
			case _ =>
				val cells = filledInCells(state)
				val gridLines = calculateGridLines(state)
				cells ++ gridLines
		}

		// put gridlines after cells so they get drawn on top
		AdjacencyMatrixViewData(shapes)
	}
}