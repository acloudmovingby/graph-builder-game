package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, AdjMatrixClickDragLogic, Clicked, DragSelecting, Hover, NoSelection}
import graphcontroller.shared.AdjMatrixCoordinateConverter
import graphcontroller.dataobject.{AdjMatrixDimensions, Cell, Point, Rectangle}
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	private val edgePresentColor = "black"
	private val hoverEdgePresentColor = "#F2813B"
	private val hoverNoEdgeColor = "#E2E2E2"
	private val clickedEdgePresentColor = "#ffb78a" // lighter shade
	private val clickedNoEdgeColor = "#f2f2f2" // hoverEdgePresentColor //

	def calculateGridLines(nodeCount: Int, dimensions: AdjMatrixDimensions): Seq[CanvasLine] = {
		val padding = dimensions.padding
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val (width, height) = (dimensions.cellWidth(nodeCount), dimensions.cellHeight(nodeCount))

			for {
				i <- 0 to nodeCount
				// first calculate lines without padding, then translate by padding afterward
				verticalLine = CanvasLine(
					from = Point(x = (width * i).toInt, y = 0),
					to = Point(x = (width * i).toInt, y = dimensions.matrixHeight),
					width = 1,
					color = "lightgray"
				)
				horizontalLine = CanvasLine(
					from = Point(x = 0, y = (height * i).toInt),
					to = Point(x = dimensions.matrixWidth, y = (height * i).toInt),
					width = 1,
					color = "lightgray"
				)
				line <- Seq(verticalLine, horizontalLine)
				// shift down and right by padding amount
				vector = Point(padding, padding)
				withPadding = line.copy(from = line.from.translate(vector), to = line.to.translate(vector))
			} yield withPadding
		}
	}

	private def hoveredCellHighlight(state: State, hoveredCell: Cell): Option[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount
		if (nodeCount == 0) None else {
			val (cellWidth, cellHeight) = (state.adjMatrixDimensions.cellWidth(nodeCount), state.adjMatrixDimensions.cellHeight(nodeCount))
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
		val gridLines = calculateGridLines(state.graph.nodeCount, state.adjMatrixDimensions)

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