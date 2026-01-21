package graphcontroller.shared

import graphcontroller.dataobject.{AdjMatrixZone, Cell, Column, Corner, NoCell, Point, Row, Rectangle}
import graphcontroller.dataobject.canvas.{RectangleCanvas, RenderOp}
import graphi.MapGraph

import scala.collection.immutable.ListSet

// TODO: I hate this name
object AdjMatrixCoordinateConverter {
	val padding = 10 // pixels of padding around the adjacency matrix

	def convertCoordinatesToZone(
		mouseX: Int,
		mouseY: Int,
		adjMatrixCanvasDimensions: (Int, Int),
		nodeCount: Int
	): AdjMatrixZone = {
		if (nodeCount == 0) return NoCell
		// padding on all 4 sides around the actual matrix. adjMatrixDimensions is the full dimensions of the canvas
		val (canvasWidth, canvasHeight) = adjMatrixCanvasDimensions
		val matrixWidth = canvasWidth.toDouble - (padding * 2.0)
		val matrixHeight = canvasHeight.toDouble - (padding * 2.0)
		val cellWidth = matrixWidth / nodeCount
		val cellHeight = matrixHeight / nodeCount
		if (mouseX < padding && mouseY < padding) {
			Corner
		} else if (mouseX < padding && mouseY >= padding && mouseY < padding + matrixHeight) {
			// in row header area
			val row = ((mouseY - padding) / cellHeight).toInt
			Row(row)
		} else if (mouseY < padding && mouseX >= padding && mouseX < padding + matrixWidth) {
			// in column header area
			val col = ((mouseX - padding) / cellWidth).toInt
			Column(col)
		} else if (mouseX >= padding && mouseX < padding + matrixWidth && mouseY >= padding && mouseY < padding + matrixHeight) {
			// in cell area
			val col = ((mouseX - padding) / cellWidth).toInt
			val row = ((mouseY - padding) / cellHeight).toInt
			Cell(row, col)
		} else {
			Corner // outside the matrix area
		}
	}

	// For rendering purposes, convert a zone to a rectangle representing its area on the canvas
	def convertZoneToShape(z: AdjMatrixZone, adjMatrixCanvasDimensions: (Int, Int), nodeCount: Int): Option[Rectangle] = {
		// if it's a cell, make a rectangle for that cell
		// if it's a row or column, make a rectangle for the whole row/column
		// if it's corner or NoCell, return None
		if (nodeCount == 0) return None
		val (canvasWidth, canvasHeight) = adjMatrixCanvasDimensions
		val matrixWidth = canvasWidth.toDouble - (padding * 2.0)
		val matrixHeight = canvasHeight.toDouble - (padding * 2.0)
		val cellWidth = matrixWidth / nodeCount
		val cellHeight = matrixHeight / nodeCount

		z match {
			case Cell(row, col) =>
				Some(Rectangle(
					topLeft = Point(
						x = (padding + col * cellWidth).toInt,
						y = (padding + row * cellHeight).toInt
					),
					width = cellWidth.toInt,
					height = cellHeight.toInt
				))
			case Row(row) =>
				Some(Rectangle(
					topLeft = Point(
						x = padding,
						y = (padding + row * cellHeight).toInt
					),
					width = matrixWidth.toInt,
					height = cellHeight.toInt
				))
			case Column(col) =>
				Some(Rectangle(
					topLeft = Point(
						x = (padding + col * cellWidth).toInt,
						y = padding
					),
					width = cellWidth.toInt,
					height = matrixHeight.toInt
				))
			case Corner | NoCell => None
		}
	}
}
