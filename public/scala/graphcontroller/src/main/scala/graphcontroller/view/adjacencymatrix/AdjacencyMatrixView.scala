package graphcontroller.view.adjacencymatrix

import graphi.MapGraph
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.{AdjMatrixInteractionState, AdjMatrixClickDragLogic, Clicked, Hover, NoSelection}
import graphcontroller.shared.AdjMatrixCoordinateConverter
import graphcontroller.dataobject.{AdjMatrixDimensions, AdjMatrixZone, Cell, Column, Rectangle, Row, Vector2D}
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp, TextCanvas}
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	val edgePresentColor = "black"
	val hoverEdgePresentColor = "#F2813B"
	val hoverNoEdgeColor = "#E2E2E2"
	val clickedEdgePresentColor = "#ffb78a" // lighter shade
	val clickedNoEdgeColor = "#f2f2f2" // hoverEdgePresentColor //
	val defaultNumberColor = "darkgray"

	def rowColNumbers(nodeCount: Int, dimensions: AdjMatrixDimensions, adjMatrixState: AdjMatrixInteractionState): Seq[TextCanvas] = {
		adjMatrixState match {
			case NoSelection => Seq.empty
			case _ =>
				val (cellWidth, cellHeight) = (dimensions.cellWidth(nodeCount), dimensions.cellHeight(nodeCount))

				// row numbers (going down left side of matrix)
				val rowNumbers = (0 until nodeCount).map { i =>
					val x = dimensions.padding - dimensions.numberPadding
					val y = (i * cellHeight).toInt + dimensions.padding + cellHeight.toInt / 2
					TextCanvas(coords = Vector2D(x, y), text = i.toString, color = defaultNumberColor, fontSize = 12)
				}

				val colNumbers = (0 until nodeCount).map { i =>
					val x = (i * cellWidth).toInt + dimensions.padding + cellWidth.toInt / 2
					val y = dimensions.padding - dimensions.numberPadding
					TextCanvas(coords = Vector2D(x, y), text = i.toString, color = defaultNumberColor, fontSize = 12)
				}

				rowNumbers ++ colNumbers
		}
	}

	def calculateGridLines(nodeCount: Int, dimensions: AdjMatrixDimensions): Seq[CanvasLine] = {
		val padding = dimensions.padding
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val (width, height) = (dimensions.cellWidth(nodeCount), dimensions.cellHeight(nodeCount))

			for {
				i <- 0 to nodeCount
				// first calculate lines without padding, then translate by padding afterward
				verticalLine = CanvasLine(
					from = Vector2D(x = (width * i).toInt, y = 0),
					to = Vector2D(x = (width * i).toInt, y = dimensions.matrixHeight),
					width = 1,
					color = "lightgray"
				)
				horizontalLine = CanvasLine(
					from = Vector2D(x = 0, y = (height * i).toInt),
					to = Vector2D(x = dimensions.matrixWidth, y = (height * i).toInt),
					width = 1,
					color = "lightgray"
				)
				lines <- Seq(verticalLine, horizontalLine)
			} yield lines
		}
	}

	def hoveredCellHighlight(graph: MapGraph[Int, ?], dimensions: AdjMatrixDimensions, hoveredZone: AdjMatrixZone): Seq[RectangleCanvas] = {
		val nodeCount = graph.nodeCount

		def hoveredCell(cell: Cell) = {
				val (cellWidth, cellHeight) = (dimensions.cellWidth(nodeCount), dimensions.cellHeight(nodeCount))
				val color = if (graph.getEdges.contains(cell.toEdge)) hoverEdgePresentColor else hoverNoEdgeColor
				RectangleCanvas(
					Rectangle(
						topLeft = Vector2D(
							x = (cell.col * cellWidth).toInt,
							y = (cell.row * cellHeight).toInt
						),
						width = cellWidth.toInt,
						height = cellHeight.toInt
					),
					color = color
				)
		}

		if (nodeCount == 0) Seq.empty else {
			hoveredZone match {
				case Cell(row, col) => Seq(hoveredCell(Cell(row, col)))
				case r: Row => r.cells(nodeCount).map(hoveredCell)
				case c: Column => c.cells(nodeCount).map(hoveredCell)
				case _ => Seq.empty // TODO implement Column or other things
			}
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
					.map(rect =>
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
				val hoveredCell = hoveredCellHighlight(state.graph, state.adjMatrixDimensions, cell)
				cells ++ hoveredCell.toSeq ++ gridLines
			case d: Clicked =>
				val selectedCells = d.selectedCells
					.filter { c =>
						// we only highlight cells that would change when we apply the selection
						(d.isAdd, state.graph.getEdges.contains(c.toEdge)) match {
							case (true, false) => true // adding an edge that doesn't exist
							case (false, true) => true // removing an edge that does exist
							case _ => false // otherwise don't change what's drawn
						}
					}
					.map { cell => clickedCellHighlight(state, cell, d.isAdd) }
				cells ++ selectedCells ++ gridLines
			case _ => cells ++ gridLines
		}

		// translate all the matrix shapes down to the right, to account for the padding
		val adjustedForPadding = shapes.map(_.translate(Vector2D(
			x = state.adjMatrixDimensions.padding,
			y = state.adjMatrixDimensions.padding
		)))

		val numbers = rowColNumbers(state.graph.nodeCount, state.adjMatrixDimensions, state.adjMatrixState)

		AdjacencyMatrixViewData(adjustedForPadding ++ numbers)
	}
}