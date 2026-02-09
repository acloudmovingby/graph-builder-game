package graphcontroller.components.adjacencymatrix

import graphcontroller.components.adjacencymatrix.{AdjMatrixInteractionLogic, AdjMatrixInteractionState, CellClicked, Hover, NoSelection}
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, CanvasRenderOp, TextCanvas}
import graphcontroller.dataobject.*
import graphcontroller.model.State
import graphcontroller.shared.{AdjMatrixCoordinateConverter, GridUtils}

object AdjacencyMatrixView {
	// TODO put this in a config file somewhere?
	val edgePresentColor = "black"
	val hoverEdgePresentColor = "#F2813B"
	val hoverNoEdgeColor = "#E2E2E2"
	val clickedEdgePresentColor = "#ffb78a" // lighter shade
	val clickedNoEdgeColor = "#f2f2f2" // hoverEdgePresentColor //
	val defaultNumberColor = "transparent"
	// TODO make this match the arrow color (it's close right now but not same)
	val highlightNumberColor = "orange" // to highlight the specific number of the row/column being hovered over
	val numberFontSize = 13

	def rowColNumbers(
		nodeCount: Int,
		dimensions: AdjMatrixDimensions,
		adjMatrixState: AdjMatrixInteractionState,
		grid: GridUtils
	): Seq[TextCanvas] = {
		adjMatrixState match {
			case NoSelection => Seq.empty
			case _ =>
				def generateColor(index: Int, isRow: Boolean): String = {
					adjMatrixState match {
						case Hover(Cell(row, col)) =>
							if (isRow && index == row) highlightNumberColor
							else if (!isRow && index == col) highlightNumberColor
							else defaultNumberColor
						case Hover(Row(row)) =>
							if (isRow && index == row) highlightNumberColor
							else if (!isRow) highlightNumberColor
							else defaultNumberColor
						case Hover(Column(col)) =>
							if (!isRow && index == col) highlightNumberColor
							else if (isRow) highlightNumberColor
							else defaultNumberColor
						case _ => defaultNumberColor
					}
				}

				// row numbers (going down left side of matrix)
				val rowNumbers = (0 until nodeCount).map { i =>
					val x = dimensions.padding - dimensions.numberPadding
					val y = grid.getY(i) + grid.getHeight(i) / 2 + dimensions.padding
					val color = generateColor(i, isRow = true)
					TextCanvas(coords = Vector2D(x, y), text = i.toString, color = color, fontSize = numberFontSize)
				}

				val colNumbers = (0 until nodeCount).map { i =>
					val x = grid.getX(i) + grid.getWidth(i) / 2 + dimensions.padding
					val y = dimensions.padding - dimensions.numberPadding
					val color = generateColor(i, isRow = false)
					TextCanvas(coords = Vector2D(x, y), text = i.toString, color = color, fontSize = numberFontSize)
				}

				rowNumbers ++ colNumbers
		}
	}

	def calculateGridLines(
		nodeCount: Int,
		dimensions: AdjMatrixDimensions,
		grid: GridUtils
	): Seq[CanvasLine] = {
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			val verticalLines = grid.colCoords.map { x =>
				CanvasLine(
					from = Vector2D(x = x, y = 0),
					to = Vector2D(x = x, y = dimensions.matrixHeight),
					width = 1,
					color = "lightgray"
				)
			}
			val horizontalLines = grid.rowCoords.map { y =>
				CanvasLine(
					from = Vector2D(x = 0, y = y),
					to = Vector2D(x = dimensions.matrixWidth, y = y),
					width = 1,
					color = "lightgray"
				)
			}
			verticalLines ++ horizontalLines
		}
	}

	/** When you hover over a cell or a row/column number, this is the shading drawn over that cell area that indicates
	 * which cell of the matrix (i.e. edge) clicking would potentially affect.  */
	def hoveredCellHighlight(
		state: State,
		hoveredZone: AdjMatrixZone,
		grid: GridUtils
	): Seq[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount

		def hoveredCell(cell: Cell): RectangleCanvas = {
			val color = if (state.graph.getEdges.contains(cell.toEdge)) hoverEdgePresentColor else hoverNoEdgeColor
			RectangleCanvas(
				Rectangle(
					topLeft = Vector2D(
						x = grid.getX(cell.col),
						y = grid.getY(cell.row)
					),
					width = grid.getWidth(cell.col),
					height = grid.getHeight(cell.row)
				),
				color = color
			)
		}

		if (nodeCount == 0) Seq.empty else {
			hoveredZone match {
				case Cell(row, col) => Seq(hoveredCell(Cell(row, col)))
				case r: Row => r.cells(nodeCount).map(hoveredCell)
				case c: Column => c.cells(nodeCount).map(hoveredCell)
				case _ => Seq.empty
			}
		}
	}

	private def clickedCellHighlight(state: State, cell: Cell, isAdd: Boolean, grid: GridUtils): RectangleCanvas = {
		val nodeCount = state.graph.nodeCount
		val color = if (isAdd) clickedNoEdgeColor else clickedEdgePresentColor
		RectangleCanvas(
			AdjMatrixCoordinateConverter.convertZoneToShape(cell, grid, nodeCount).get,
			color = color
		)
	}

	/** Render data for matrix cells representing existing edges (at time of writing, these are the black squares) */
	private def filledInCells(state: State, grid: GridUtils): Seq[RectangleCanvas] = {
		val nodeCount = state.graph.nodeCount
		// check this first to avoid division by zero
		if (nodeCount == 0) Seq.empty else {
			state.graph.getEdges.toSeq.flatMap { case (from, to) =>
				AdjMatrixCoordinateConverter.convertZoneToShape(Cell(from, to), grid, nodeCount)
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
		val nodeCount = state.graph.nodeCount
		val dimensions = state.adjMatrixDimensions
		val grid = GridUtils(dimensions.matrixWidth, dimensions.matrixHeight, nodeCount)

		val cells = filledInCells(state, grid)
		val gridLines = calculateGridLines(nodeCount, dimensions, grid)

		val shapes: Seq[CanvasRenderOp] = state.adjMatrixState match {
			case NoSelection => // fill in cells only, no grid lines
				cells
			case Hover(cell) => // fill in cells + hovered cell highlight + grid lines
				val hoveredCell = hoveredCellHighlight(state, cell, grid)
				cells ++ hoveredCell ++ gridLines
			case d: CellClicked =>
				val selectedCells = d.selectedCells
					.filter { c =>
						// we only highlight cells that would change when we apply the selection
						(d.isAdd, state.graph.getEdges.contains(c.toEdge)) match {
							case (true, false) => true // adding an edge that doesn't exist
							case (false, true) => true // removing an edge that does exist
							case _ => false // otherwise don't change what's drawn
						}
					}
					.map { cell => clickedCellHighlight(state, cell, d.isAdd, grid) }
				cells ++ selectedCells ++ gridLines
			case _ => cells ++ gridLines
		}

		// translate all the matrix shapes down to the right, to account for the padding
		val adjustedForPadding = shapes.map(_.translate(Vector2D(
			x = dimensions.padding,
			y = dimensions.padding
		)))

		val numbers = rowColNumbers(nodeCount, dimensions, state.adjMatrixState, grid)

		AdjacencyMatrixViewData(adjustedForPadding ++ numbers)
	}
}