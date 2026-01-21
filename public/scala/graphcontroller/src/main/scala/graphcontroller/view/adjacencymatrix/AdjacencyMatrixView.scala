package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, AdjMatrixClickDragLogic, Clicked, DragSelecting, Hover, NoSelection}
import graphcontroller.shared.AdjMatrixCoordinateConverter
import AdjMatrixClickDragLogic.padding
import graphcontroller.dataobject.{Cell, Point, Rectangle}
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	private val edgePresentColor = "black"
	private val hoverEdgePresentColor = "#F2813B"
	private val hoverNoEdgeColor = "#E2E2E2"
	private val clickedEdgePresentColor = "#ffb78a" // lighter shade
	private val clickedNoEdgeColor = "#f2f2f2" // hoverEdgePresentColor //

	/** Use Floats for intermediate calculations so we don't get rounding errors when dividing then multiplying again later */
	def cellWidthHeight(nodeCount: Int, adjMatrixDimensions: (Int, Int)): (Float, Float) = {
		if (nodeCount == 0) (0, 0)
		else {
			val cellWidth = (adjMatrixDimensions._1 - (padding * 2)).toFloat / nodeCount.toFloat
			val cellHeight = (adjMatrixDimensions._2 - (padding * 2)).toFloat / nodeCount.toFloat
			(cellWidth, cellHeight)
		}
	}

	private def calculateGridLines(state: State): Seq[CanvasLine] = {
		val nodeCount = state.graph.nodeCount
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val (width, height) = cellWidthHeight(nodeCount, state.adjMatrixDimensions)

			for {
				i <- 1 until nodeCount
				verticalLine = CanvasLine(
					from = Point(x = (width * i).toInt, y = 0),
					to = Point(x = (width * i).toInt, y = (state.adjMatrixDimensions._2 - (padding * 2))),
					width = 1,
					color = "lightgray"
				)
				horizontalLine = CanvasLine(
					from = Point(x = 0, y = (height * i).toInt),
					to = Point(x = (state.adjMatrixDimensions._1 - (padding * 2)), y = (height * i).toInt),
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
			val (cellWidth, cellHeight) = cellWidthHeight(nodeCount, state.adjMatrixDimensions)
			val color = if (state.graph.getEdges.contains(hoveredCell.toEdge)) hoverEdgePresentColor else hoverNoEdgeColor
			Some(RectangleCanvas(
				Rectangle(
					topLeft = Point(
						x = (hoveredCell.col * cellWidth).toInt,
						y = (hoveredCell.row * cellHeight).toInt
					),
					width = cellWidth.toInt,
					height = cellHeight.toInt
				),
				color = color
			))
		}
	}

	private def clickedCellHighlight(state: State, cell: Cell, isAdd: Boolean): RectangleCanvas = {
		val nodeCount = state.graph.nodeCount
		val (cellWidth, cellHeight) = cellWidthHeight(nodeCount, state.adjMatrixDimensions)
		val color = if (isAdd) clickedNoEdgeColor else clickedEdgePresentColor
		RectangleCanvas(
			AdjMatrixCoordinateConverter.convertZoneToShape(cell, state.adjMatrixDimensions, nodeCount).get,
			color = color
		)
	}

	/** Render data for matrix cells representing existing edges */
	private def filledInCells(state: State): Seq[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val (cellWidth, cellHeight) = cellWidthHeight(nodeCount, state.adjMatrixDimensions)
			state.graph.getEdges.toSeq.flatMap { case (from, to) =>
				AdjMatrixCoordinateConverter.convertZoneToShape(Cell(from, to), state.adjMatrixDimensions, nodeCount)
					.map( rect =>
						RectangleCanvas(
							rect,
							color = edgePresentColor
						)
					).toSeq
			}
		}
	}

	def render(state: State): AdjacencyMatrixViewData = {

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