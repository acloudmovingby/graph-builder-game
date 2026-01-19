package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, Cell, Clicked, DragSelecting, Hover, NoSelection}
import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	private val edgePresentColor = "black"
	private val hoverEdgePresentColor = "#F2813B"
	private val hoverNoEdgeColor = "#E2E2E2"
	private val clickedEdgePresentColor = "#ffb78a"  // lighter shade
	private val clickedNoEdgeColor = "#f2f2f2" // hoverEdgePresentColor //

	private var _temp: AdjMatrixInteractionState = State.init.adjMatrixState


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

	private def clickedCellHighlight(state: State, cell: Cell, isAdd: Boolean): RectangleCanvas = {
		val nodeCount = state.graph.nodeCount
		val cellWidth = state.adjMatrixDimensions._1 / nodeCount
		val cellHeight = state.adjMatrixDimensions._2 / nodeCount
		val color = if (isAdd) clickedNoEdgeColor else clickedEdgePresentColor
		RectangleCanvas(
			x = cell.col * cellWidth,
			y = cell.row * cellHeight,
			width = cellWidth,
			height = cellHeight,
			color = color
		)
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

		if (_temp != state.adjMatrixState) {
			_temp = state.adjMatrixState
			println(s"${state.adjMatrixState}, ${state.adjMatrixDimensions}")
		}

		val cells = filledInCells(state)
		val gridLines = calculateGridLines(state)

		val shapes: Seq[RenderOp] = state.adjMatrixState match {
			case NoSelection => // fill in cells only, no grid lines
				cells
			case Hover(cell) => // fill in cells + hovered cell highlight + grid lines
				val hoveredCell = hoveredCellHighlight(state, cell)
				cells ++ hoveredCell.toSeq ++ gridLines
			case Clicked(startCell, isAdd) => // fill in cells + clicked cell highlight + grid lines
				val clickedCell = clickedCellHighlight(state, startCell, isAdd)
				cells ++ Seq(clickedCell) ++ gridLines
			case d: DragSelecting =>
				val selectedCells = d.selectedCells.map { cell => clickedCellHighlight(state, cell, d.isAdd) }
				cells ++ selectedCells ++ gridLines
			case _ => cells ++ gridLines
		}

		// put gridlines after cells so they get drawn on top
		AdjacencyMatrixViewData(shapes)
	}
}