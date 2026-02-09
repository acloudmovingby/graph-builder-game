package graphcontroller.shared

import graphcontroller.dataobject.{AdjMatrixDimensions, AdjMatrixZone, Cell, Column, Corner, NoCell, Vector2D, Row, Rectangle}
import graphi.MapGraph

import scala.collection.immutable.ListSet

object AdjMatrixCoordinateConverter {

	/**
	 * When the user clicks in the adjacency matrix canvas area, determine which zone they are clicking on (a cell in the
	 * matrix itself, or in one of the padded areas around the matrix)
	 */
	def convertCoordinatesToZone(
		mouseX: Int,
		mouseY: Int,
		dimensions: AdjMatrixDimensions,
		nodeCount: Int,
		grid: GridUtils
	): AdjMatrixZone = {
		if (nodeCount == 0) return NoCell

		val padding = dimensions.padding
		val matrixWidth = dimensions.matrixWidth
		val matrixHeight = dimensions.matrixHeight

		val x = mouseX - padding
		val y = mouseY - padding

		if (mouseX < padding && mouseY < padding) {
			Corner
		} else if (mouseX < padding && y >= 0 && y < matrixHeight) {
			val row = grid.rowCoords.lastIndexWhere(_ <= y)
			if (row >= 0 && row < nodeCount) Row(row) else Corner
		} else if (mouseY < padding && x >= 0 && x < matrixWidth) {
			val col = grid.colCoords.lastIndexWhere(_ <= x)
			if (col >= 0 && col < nodeCount) Column(col) else Corner
		} else if (x >= 0 && x < matrixWidth && y >= 0 && y < matrixHeight) {
			val col = grid.colCoords.lastIndexWhere(_ <= x)
			val row = grid.rowCoords.lastIndexWhere(_ <= y)
			if (col >= 0 && col < nodeCount && row >= 0 && row < nodeCount) Cell(row, col) else Corner
		} else {
			Corner // outside the matrix area
		}
	}

	// For rendering purposes, convert a zone to a rectangle representing its area on the _matrix_ part of the canvas
	// therefore IGNORES PADDING. So a Row will be a rectangle spanning the full width of the matrix area
	def convertZoneToShape(
		z: AdjMatrixZone,
		grid: GridUtils,
		nodeCount: Int
	): Option[Rectangle] = {
		if (nodeCount == 0) return None

		z match {
			case Corner | NoCell => None
			case Cell(row, col) =>
				Some(Rectangle(
					topLeft = Vector2D(
						x = grid.getX(col),
						y = grid.getY(row)
					),
					width = grid.getWidth(col),
					height = grid.getHeight(row)
				))
			case Row(row) =>
				Some(Rectangle(
					topLeft = Vector2D(
						x = 0,
						y = grid.getY(row)
					),
					width = grid.width,
					height = grid.getHeight(row)
				))
			case Column(col) =>
				Some(Rectangle(
					topLeft = Vector2D(
						x = grid.getX(col),
						y = 0
					),
					width = grid.getWidth(col),
					height = grid.height
				))
		}
	}
}
