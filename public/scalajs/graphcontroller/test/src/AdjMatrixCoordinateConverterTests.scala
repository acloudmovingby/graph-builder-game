import utest.*

import graphcontroller.shared.{AdjMatrixCoordinateConverter, GridUtils}
import graphcontroller.dataobject.{AdjMatrixZone, AdjMatrixDimensions, Cell, Column, Corner, NoCell, Row}

object AdjMatrixCoordinateConverterTests extends TestSuite {
	def tests = Tests {
		val padding = 5
		// 100x100 matrix area with 5 padding on each side
		val dimensions = AdjMatrixDimensions(
			canvasWidth = 110,
			canvasHeight = 110,
			padding = padding
		)

		val matrixWidth = dimensions.matrixWidth
		val matrixHeight = dimensions.matrixHeight
		val nodeCount = 5 // matrix will be 5x5
		val grid = GridUtils(matrixWidth, matrixHeight, nodeCount)
		def cellSize = matrixWidth / nodeCount // for rough coordinate checking

		def convertCoordinatesToZone(
			mouseX: Int,
			mouseY: Int
		) = AdjMatrixCoordinateConverter.convertCoordinatesToZone(
			mouseX,
			mouseY,
			dimensions,
			nodeCount,
			grid
		)

		def convertZoneToShape(zone: AdjMatrixZone) = AdjMatrixCoordinateConverter.convertZoneToShape(
			zone,
			grid = grid,
			nodeCount = nodeCount
		)

		test("top left cell") {
			val result = convertCoordinatesToZone(
				mouseX = padding + (cellSize / 2),
				mouseY = padding + (cellSize / 2)
			)
			assert(result == Cell(0, 0))
		}
		test("top right cell") {
			val result = convertCoordinatesToZone(
				mouseX = padding + matrixWidth - (cellSize / 2),
				mouseY = padding + (cellSize / 2)
			)
			assert(result == Cell(0, 4))
		}
		test("bottom left cell") {
			val result = convertCoordinatesToZone(
				mouseX = padding + (cellSize / 2),
				mouseY = padding + matrixHeight - (cellSize / 2)
			)
			assert(result == Cell(4, 0))
		}
		test("bottom right cell") {
			val result = convertCoordinatesToZone(
				mouseX = padding + matrixWidth - (cellSize / 2),
				mouseY = padding + matrixHeight - (cellSize / 2)
			)
			assert(result == Cell(4, 4))
		}
		test("Row 0 (first row)") {
			val result = convertCoordinatesToZone(
				mouseX = padding / 2,
				mouseY = padding + (cellSize / 2)
			)
			assert(result == Row(0))
		}
		test("Row 1 (second row)") {
			val result = convertCoordinatesToZone(
				mouseX = padding / 2,
				mouseY = padding + cellSize + (cellSize / 2)
			)
			assert(result == Row(1))
		}
		test("Column 0 (first column)") {
			val result = convertCoordinatesToZone(
				mouseX = padding + (cellSize / 2),
				mouseY = padding / 2
			)
			assert(result == Column(0))
		}
		test("Column 1 (second column)") {
			val result = convertCoordinatesToZone(
				mouseX = padding + cellSize + (cellSize / 2),
				mouseY = padding / 2
			)
			assert(result == Column(1))
		}
		test("Corner (upper left)") {
			val result = convertCoordinatesToZone(
				mouseX = padding / 2,
				mouseY = padding / 2
			)
			assert(result == Corner)
		}
		test("NoCell when nodeCount is 0") {
			val zeroGrid = GridUtils(dimensions.matrixWidth, dimensions.matrixHeight, 0)
			val result = AdjMatrixCoordinateConverter.convertCoordinatesToZone(
				mouseX = 50,
				mouseY = 50,
				dimensions,
				nodeCount = 0,
				grid = zeroGrid
			)
			assert(result == NoCell)
		}
		test("Corner (upper right)") {
			val result = convertCoordinatesToZone(
				mouseX = padding + matrixWidth + (padding / 2),
				mouseY = padding / 2
			)
			assert(result == Corner)
		}
		test("Corner (bottom left)") {
			val result = convertCoordinatesToZone(
				mouseX = padding / 2,
				mouseY = padding + matrixHeight + (padding / 2)
			)
			assert(result == Corner)
		}
		test("Corner (bottom right)") {
			val result = convertCoordinatesToZone(
				mouseX = padding + matrixWidth + (padding / 2),
				mouseY = padding + matrixHeight + (padding / 2)
			)
			assert(result == Corner)
		}

		test("convertZoneToShape for Cell") {
			val row = 2
			val col = 3
			val zone = Cell(row, col)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			assert(rect.topLeft.x == grid.getX(col))
			assert(rect.topLeft.y == grid.getY(row))
			assert(rect.width == grid.getWidth(col))
			assert(rect.height == grid.getHeight(row))
		}
		test("convertZoneToShape for Row") {
			val row = 1
			val zone = Row(row)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			assert(rect.topLeft.x == 0)
			assert(rect.topLeft.y == grid.getY(row))
			assert(rect.width == matrixWidth)
			assert(rect.height == grid.getHeight(row))
		}
		test("convertZoneToShape for Column") {
			val col = 4
			val zone = Column(col)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			assert(rect.topLeft.x == grid.getX(col))
			assert(rect.topLeft.y == 0)
			assert(rect.width == grid.getWidth(col))
			assert(rect.height == matrixHeight)
		}
		test("convertZoneToShape for Corner returns None") {
			val zone = Corner
			val result = convertZoneToShape(zone)
			assert(result.isEmpty)
		}
		test("convertZoneToShape returns None when nodeCount is 0") {
			val zone = Cell(0, 0)
			val zeroGrid = GridUtils(dimensions.matrixWidth, dimensions.matrixHeight, 0)
			val result = AdjMatrixCoordinateConverter.convertZoneToShape(
				zone,
				grid = zeroGrid,
				nodeCount = 0
			)
			assert(result.isEmpty)
		}
	}
}