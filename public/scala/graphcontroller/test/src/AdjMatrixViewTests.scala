import graphcontroller.dataobject.*
import graphcontroller.view.adjacencymatrix.AdjacencyMatrixView
import graphi.DirectedMapGraph
import utest.*

object AdjMatrixViewTests extends TestSuite {
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

		test("AdjMatrixDimensions matrix size calculations") {
			assert(matrixWidth == 100)
			assert(matrixHeight == 100)
		}

		test("calculateGridLines - 5 nodes") {
			val nodeCount = 5 // matrix will be 5x5 with each cell 20x20 pixels
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions)
			println("Lines: " + lines.map(line => s"(${line.from.x}, ${line.from.y})  -> (${line.to.x}, ${line.to.y})").mkString("\n"))
			// there should be 12 lines (6 vertical, 6 horizontal) since we draw lines on the edge as well
			assert(lines.length == 12)
			// check positions of first vertical and horizontal lines
			val firstVerticalLine = lines.find(line => line.from.x == padding && line.to.x == padding).get
			assert(firstVerticalLine.from.y == padding)
			assert(firstVerticalLine.to.y == matrixHeight + padding)

			val firstHorizontalLine = lines.find(line => line.from.y == padding && line.to.y == padding).get
			assert(firstHorizontalLine.from.x == padding)
			assert(firstHorizontalLine.to.x == matrixWidth + padding)

			val lastVerticalLine = lines.find(line => line.from.x == matrixWidth + padding && line.to.x == matrixWidth + padding).get
			assert(lastVerticalLine.from.y == padding)
			assert(lastVerticalLine.to.y == matrixHeight + padding)

			val lastHorizontalLine = lines.find(line => line.from.y == matrixHeight + padding && line.to.y == matrixHeight + padding).get
			assert(lastHorizontalLine.from.x == padding)
			assert(lastHorizontalLine.to.x == matrixWidth + padding)
		}

		test("Zero width padding") {
			val zeroPaddingDimensions = AdjMatrixDimensions(
				canvasWidth = 100,
				canvasHeight = 100,
				padding = 0
			)
			assert(zeroPaddingDimensions.matrixWidth == 100)
			assert(zeroPaddingDimensions.matrixHeight == 100)

			val nodeCount = 4
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, zeroPaddingDimensions)
			// there should be 10 lines (5 vertical, 5 horizontal)
			assert(lines.length == 10)
			// check first vertical line
			val firstVerticalLine = lines.find(line => line.from.x == 0 && line.to.x == 0).get
			assert(firstVerticalLine.from.y == 0)
			assert(firstVerticalLine.to.y == 100)

			// check first horizontal line
			val firstHorizontalLine = lines.find(line => line.from.y == 0 && line.to.y == 0).get
			assert(firstHorizontalLine.from.x == 0)
			assert(firstHorizontalLine.to.x == 100)

			// check last vertical line
			val lastVerticalLine = lines.find(line => line.from.x == 100 && line.to.x == 100).get
			assert(lastVerticalLine.from.y == 0)
			assert(lastVerticalLine.to.y == 100)

			// check last horizontal line
			val lastHorizontalLine = lines.find(line => line.from.y == 100 && line.to.y == 100).get
			assert(lastHorizontalLine.from.x == 0)
			assert(lastHorizontalLine.to.x == 100)
		}

		test("Zero nodes") {
			val nodeCount = 0
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions)
			// there should be no lines when there are zero nodes
			// TODO: consider if this is true or if we want to include the border lines
			assert(lines.isEmpty)
		}

		test("One node") {
			val nodeCount = 1
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions)
			// there should be 4 lines (2 vertical, 2 horizontal)
			assert(lines.length == 4)
			// check positions of lines
			val leftVerticalLine = lines.find(line => line.from.x == padding && line.to.x == padding).get
			assert(leftVerticalLine.from.y == padding)
			assert(leftVerticalLine.to.y == matrixHeight + padding)

			val rightVerticalLine = lines.find(line => line.from.x == matrixWidth + padding && line.to.x == matrixWidth + padding).get
			assert(rightVerticalLine.from.y == padding)
			assert(rightVerticalLine.to.y == matrixHeight + padding)

			val topHorizontalLine = lines.find(line => line.from.y == padding && line.to.y == padding).get
			assert(topHorizontalLine.from.x == padding)
			assert(topHorizontalLine.to.x == matrixWidth + padding)

			val bottomHorizontalLine = lines.find(line => line.from.y == matrixHeight + padding && line.to.y == matrixHeight + padding).get
			assert(bottomHorizontalLine.from.x == padding)
			assert(bottomHorizontalLine.to.x == matrixWidth + padding)
		}

		test("hoveredCellHighlight") {
			val graph = DirectedMapGraph[Int]()
				.addNode(0)
				.addNode(1)
				.addEdge(0, 1)

			val hoveredCellWithEdge = Cell(0, 1) // there is an edge from 0 to 1
			val hoveredCellWithoutEdge = Cell(1, 0) // no edge from 1 to 0

			val highlightWithEdge = AdjacencyMatrixView.hoveredCellHighlight(graph, dimensions, hoveredCellWithEdge)
			assert(highlightWithEdge.isDefined)
			assert(highlightWithEdge.get.color == AdjacencyMatrixView.hoverEdgePresentColor)
			assert(highlightWithEdge.get.rect == Rectangle(
				topLeft = Vector2D(x = (hoveredCellWithEdge.col * dimensions.cellWidth(graph.nodeCount)).toInt,
					y = (hoveredCellWithEdge.row * dimensions.cellHeight(graph.nodeCount)).toInt),
				width = dimensions.cellWidth(graph.nodeCount).toInt,
				height = dimensions.cellHeight(graph.nodeCount).toInt
			))

			val highlightWithoutEdge = AdjacencyMatrixView.hoveredCellHighlight(graph, dimensions, hoveredCellWithoutEdge)
			assert(highlightWithoutEdge.isDefined)
			assert(highlightWithoutEdge.get.color == AdjacencyMatrixView.hoverNoEdgeColor)
		}
	}
}