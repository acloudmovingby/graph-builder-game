import graphcontroller.components.adjacencymatrix.{AdjacencyMatrixView, Hover, NoSelection}
import graphcontroller.dataobject.*
import graphcontroller.model.State
import graphcontroller.shared.{BasicTool, GraphRepresentation, GridUtils}
import graphi.DirectedMapGraph
import utest.*
import graphcontroller.components.exportpane.ExportFormat

object AdjMatrixViewTests extends TestSuite {
	def tests = Tests {
		val padding = 5
		// 100x100 matrix area with 5 padding on each side
		val dimensions = AdjMatrixDimensions(
			canvasWidth = 110,
			canvasHeight = 110,
			padding = padding,
			numberPadding = 2
		)

		val matrixWidth = dimensions.matrixWidth
		val matrixHeight = dimensions.matrixHeight

		test("AdjMatrixDimensions matrix size calculations") {
			assert(matrixWidth == 100)
			assert(matrixHeight == 100)
		}

		test("calculateGridLines - 5 nodes") {
			val nodeCount = 5 // matrix will be 5x5
			val grid = GridUtils(matrixWidth, matrixHeight, nodeCount)
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions, grid)
			// there should be 12 lines (6 vertical, 6 horizontal) since we draw lines on the edge as well
			assert(lines.length == 12)
			// check positions of first vertical and horizontal lines
			val firstVerticalLine = lines.find(line => line.from.x == 0 && line.to.x == 0).get
			assert(firstVerticalLine.from.y == 0)
			assert(firstVerticalLine.to.y == matrixHeight)

			val firstHorizontalLine = lines.find(line => line.from.y == 0 && line.to.y == 0).get
			assert(firstHorizontalLine.from.x == 0)
			assert(firstHorizontalLine.to.x == matrixWidth)

			val lastVerticalLine = lines.find(line => line.from.x == matrixWidth && line.to.x == matrixWidth).get
			assert(lastVerticalLine.from.y == 0)
			assert(lastVerticalLine.to.y == matrixHeight)

			val lastHorizontalLine = lines.find(line => line.from.y == matrixHeight && line.to.y == matrixHeight).get
			assert(lastHorizontalLine.from.x == 0)
			assert(lastHorizontalLine.to.x == matrixWidth)
		}

		test("Zero width padding") {
			val zeroPaddingDimensions = AdjMatrixDimensions(
				canvasWidth = 100,
				canvasHeight = 100,
				padding = 0,
				numberPadding = 0
			)
			assert(zeroPaddingDimensions.matrixWidth == 100)
			assert(zeroPaddingDimensions.matrixHeight == 100)

			val nodeCount = 4
			val grid = GridUtils(zeroPaddingDimensions.matrixWidth, zeroPaddingDimensions.matrixHeight, nodeCount)
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, zeroPaddingDimensions, grid)
			// there should be 10 lines (5 vertical, 5 horizontal)
			assert(lines.length == 10)
		}

		test("Zero nodes") {
			val nodeCount = 0
			val grid = GridUtils(matrixWidth, matrixHeight, nodeCount)
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions, grid)
			assert(lines.isEmpty)
		}

		test("One node") {
			val nodeCount = 1
			val grid = GridUtils(matrixWidth, matrixHeight, nodeCount)
			val lines = AdjacencyMatrixView.calculateGridLines(nodeCount, dimensions, grid)
			// there should be 4 lines (2 vertical, 2 horizontal)
			assert(lines.length == 4)
		}

		test("hoveredCellHighlight") {
			val graph = DirectedMapGraph[Int]()
				.addNode(0)
				.addNode(1)
				.addEdge(0, 1)

			val state = State(
				graph = graph,
				keyToData = Map.empty,
				undoStack = List.empty,
				adjMatrixState = NoSelection,
				adjMatrixDimensions = dimensions,
				exportFormat = ExportFormat.DOT,
				adjacencyExportType = GraphRepresentation.List,
				toolState = BasicTool(None),
				hoveringOnNode = None
			)
			val grid = GridUtils(dimensions.matrixWidth, dimensions.matrixHeight, graph.nodeCount)

			val hoveredCellWithEdge = Cell(0, 1) // there is an edge from 0 to 1
			val hoveredCellWithoutEdge = Cell(1, 0) // no edge from 1 to 0

			val highlightWithEdge = AdjacencyMatrixView.hoveredCellHighlight(state, hoveredCellWithEdge, grid)
			assert(highlightWithEdge.nonEmpty)
			assert(highlightWithEdge.head.color == AdjacencyMatrixView.hoverEdgePresentColor)
			assert(highlightWithEdge.head.rect == Rectangle(
				topLeft = Vector2D(x = grid.getX(1), y = grid.getY(0)),
				width = grid.getWidth(1),
				height = grid.getHeight(0)
			))

			val highlightWithoutEdge = AdjacencyMatrixView.hoveredCellHighlight(state, hoveredCellWithoutEdge, grid)
			assert(highlightWithoutEdge.nonEmpty)
			assert(highlightWithoutEdge.head.color == AdjacencyMatrixView.hoverNoEdgeColor)
		}

		test("row/column numbers") {
			val nodeCount = 4 // choose number divisible into matrix width/height for easier calculation
			val grid = GridUtils(matrixWidth, matrixHeight, nodeCount)
			val rowNumbers = AdjacencyMatrixView.rowColNumbers(nodeCount, dimensions, Hover(Cell(0, 0)), grid)
			assert(rowNumbers.length == nodeCount * 2)

			// assert that there are nodeCOunt number of row numbers where the x position is padding-numberPadding
			// and y position is padding + row * cellHeight + cellHeight/2
			for (i <- 0 until nodeCount) {
				val rowNumber = rowNumbers.find(rn => rn.text == i.toString && rn.coords.x == padding - dimensions.numberPadding).get
				val expectedY = grid.getY(i) + grid.getHeight(i) / 2 + padding
				assert(rowNumber.coords.y == expectedY)
			}
		}

		test("Hover over row highlights all cells in that row") {
			val graph = DirectedMapGraph[Int]()
				.addNode(0)
				.addNode(1)
				.addNode(2)
				.addEdge(0, 1)
				.addEdge(0, 2)
			
			val state = State(
				graph = graph,
				keyToData = Map.empty,
				undoStack = List.empty,
				adjMatrixState = NoSelection,
				adjMatrixDimensions = dimensions,
				exportFormat = ExportFormat.DOT,
				adjacencyExportType = GraphRepresentation.List,
				toolState = BasicTool(None),
				hoveringOnNode = None
			)
			val grid = GridUtils(dimensions.matrixWidth, dimensions.matrixHeight, graph.nodeCount)
			val hoveredRow = Row(0)

			val highlights = AdjacencyMatrixView.hoveredCellHighlight(state, hoveredRow, grid)
			assert(highlights.length == 3) // should highlight cells (0,0), (0,1), (0,2)

			val highlightedCells = highlights.map { rect =>
				val col = grid.colCoords.lastIndexWhere(_ <= rect.rect.topLeft.x)
				val row = grid.rowCoords.lastIndexWhere(_ <= rect.rect.topLeft.y)
				Cell(row, col)
			}.toSet
			
			val expectedCells = Set(Cell(0, 0), Cell(0, 1), Cell(0, 2))
			assert(highlightedCells == expectedCells)
		}
	}
}