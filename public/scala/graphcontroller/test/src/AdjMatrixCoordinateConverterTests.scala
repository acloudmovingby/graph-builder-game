import utest.*

import graphcontroller.shared.AdjMatrixCoordinateConverter
import graphcontroller.dataobject.{AdjMatrixZone, Cell, Column, Corner, NoCell, Row}
import AdjMatrixCoordinateConverter.padding

object AdjMatrixCoordinateConverterTests extends TestSuite {
	def tests = Tests {
		val matrixWidth = 100
		val matrixHeight = 100
		val nodeCount = 5
		def cellSize = matrixWidth / nodeCount // 20 pixels

		def convertCoordinatesToZone(
			mouseX: Int,
			mouseY: Int
		) = AdjMatrixCoordinateConverter.convertCoordinatesToZone(
			mouseX,
			mouseY,
			adjMatrixCanvasDimensions = (padding * 2 + matrixWidth, padding * 2 + matrixHeight), // 100x100 matrix area with 5 padding on each side
			nodeCount = nodeCount // matrix is 5x5 with each cell 20x20 pixels
		)

		def convertZoneToShape(zone: AdjMatrixZone) = AdjMatrixCoordinateConverter.convertZoneToShape(
			zone,
			adjMatrixCanvasDimensions = (padding * 2 + matrixWidth, padding * 2 + matrixHeight),
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
			val result = AdjMatrixCoordinateConverter.convertCoordinatesToZone(
				mouseX = 50,
				mouseY = 50,
				adjMatrixCanvasDimensions = (120, 120),
				nodeCount = 0
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
			val zone = Cell(2, 3)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			val expectedX = padding + (3 * cellSize)
			val expectedY = padding + (2 * cellSize)
			assert(rect.topLeft.x == expectedX)
			assert(rect.topLeft.y == expectedY)
			assert(rect.width == cellSize)
			assert(rect.height == cellSize)
		}
		test("convertZoneToShape for Row") {
			val zone = Row(1)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			val expectedX = padding
			val expectedY = padding + (1 * cellSize)
			assert(rect.topLeft.x == expectedX)
			assert(rect.topLeft.y == expectedY)
			assert(rect.width == matrixWidth)
			assert(rect.height == cellSize)
		}
		test("convertZoneToShape for Column") {
			val zone = Column(4)
			val result = convertZoneToShape(zone)
			assert(result.isDefined)
			val rect = result.get
			val expectedX = padding + (4 * cellSize)
			val expectedY = padding
			assert(rect.topLeft.x == expectedX)
			assert(rect.topLeft.y == expectedY)
			assert(rect.width == cellSize)
			assert(rect.height == matrixHeight)
		}
		test("convertZoneToShape for Corner returns None") {
			val zone = Corner
			val result = convertZoneToShape(zone)
			assert(result.isEmpty)
		}
		test("convertZoneToShape returns None when nodeCount is 0") {
			val zone = Cell(0, 0)
			val result = AdjMatrixCoordinateConverter.convertZoneToShape(
				zone,
				adjMatrixCanvasDimensions = (120, 120),
				nodeCount = 0
			)
			assert(result.isEmpty)
		}
	}
}