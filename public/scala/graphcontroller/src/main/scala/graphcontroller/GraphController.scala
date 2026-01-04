package graphcontroller

import scala.scalajs.js
import js.JSConverters.*
import scala.scalajs.js.annotation.*
import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.render.{ArrowTipRender, EdgeRender, EdgeStyle}
import graphcontroller.dataobject.{KeyWithData, KeyWithDataConverter, Line, NodeData, NodeDataJS, Point}
import graphcontroller.dataobject.canvas.{CanvasLine, CanvasLineJS, MultiShapesCanvas, MultiShapesCanvasJS, RenderOp, TriangleCanvas, TriangleCanvasJS}
import graphcontroller.render.EdgeRender.{edgeHighlightColor, potentialArrowColor, potentialEdgeStrokeColor, simpleEdgeStrokeColor, simpleEdgeStrokeWidth}
import graphcontroller.render.EdgeStyle.{Directed, DirectedHighlighted, Simple, SimpleHighlighted}
import graphcontroller.render.MainCanvas
import graphcontroller.render.properties.ArrowRenderProperties

case class GraphState[A](graph: DirectedMapGraph[A] | SimpleMapGraph[A], keyToData: Map[A, NodeData])

object GraphState {
	// Limit the number of undo states to avoid excessive memory usage
	val UNDO_SIZE_LIMIT = 50
}

@JSExportTopLevel("GraphController")
class GraphController {
	private var graph: DirectedMapGraph[Int] | SimpleMapGraph[Int] = new DirectedMapGraph[Int]() // key is Int, data is NodeData
	private var keyToData = Map[Int, NodeData]()
	private val undoGraphStates = scala.collection.mutable.Stack[GraphState[Int]]()
	private var matrixHoverCell: Option[(Int, Int)] = None
	private def hoveredEdge: Option[(Int, Int)] = matrixHoverCell.flatMap { case (from, to) =>
		if (graph.hasEdge(from, to)) Some((from, to)) else None
	}

	@JSExport
	def isDirected(): Boolean = graph match {
		case _: DirectedMapGraph[Int] => true
		case _: SimpleMapGraph[Int] => false
	}

	@JSExport
	def clearGraph(): Unit = {
		graph = new DirectedMapGraph[Int]()
		keyToData = Map.empty
	}

	@JSExport
	def nodeCount(): Int = graph.nodeCount

	@JSExport
	def edgeCount(): Int = graph.edgeCount

	@JSExport
	def addNode(key: Int, data: NodeDataJS): Unit = {
		graph = graph.addNode(key)
		keyToData += (key -> NodeData.fromJS(data))
	}

	@JSExport
	def addEdge(to: Int, from: Int): Unit = try {
		graph = graph.addEdge(to, from)
	} catch {
		case e: NoSuchElementException => println(s"Error adding edge (not yet implemented): ${e.getMessage}")
	}

	@JSExport
	def updateNodeData(key: Int, data: NodeDataJS): Unit = {
		keyToData.get(key) match {
			case Some(_) => keyToData += (key -> NodeData.fromJS(data))
			case None => println(s"Error updating node data: Node $key does not exist")
		}
	}

	@JSExport
	def pushUndoState(): Unit = {
		val nodeCopyFunction = (i: Int) => i // identity function for Int keys
		undoGraphStates.push(GraphState(graph.clone(nodeCopyFunction), keyToData)) // clone keyToData too?
		// if we exceed the limit, remove the oldest state
		if (undoGraphStates.size > GraphState.UNDO_SIZE_LIMIT) {
			undoGraphStates.remove(0)
		}
	}

	/**	No-op if no states in undo stack */
	@JSExport
	def popUndoState(): Unit = {
		if (undoGraphStates.nonEmpty) {
			val prevState = undoGraphStates.pop()
			graph = prevState.graph
			keyToData = prevState.keyToData
		}
	}

	/** For graying-out the undo button if can't undo anymore */
	@JSExport
	def canUndo(): Boolean = undoGraphStates.nonEmpty

	@JSExport
	def getDot: String = graph.toDot

	@JSExport
	def getAdjList(): js.Array[js.Array[Int]] = graph.adjMap.map(_._2.toSeq.toJSArray).toJSArray

	private def getEdgeCoordinates(fromIndex: Int, toIndex: Int): Option[Line] = for {
		fromData <- keyToData.get(fromIndex)
		toData <- keyToData.get(toIndex)
	} yield Line(
		from = Point(fromData.x, fromData.y),
		to = Point(toData.x, toData.y)
	)

	private def getEdgeObjects(g: DirectedMapGraph[Int] | SimpleMapGraph[Int]): Seq[Line] =
		g.getEdges.toSeq.flatMap { case (from, to) => getEdgeCoordinates(from, to).toSeq }

	@JSExport
	def getAdjacencyMatrix(): js.Array[js.Array[Int]] = {
		// trigger draw on canvas as test TODO: delete this
		MainCanvas.start()

		val size = graph.nodeCount
		// initialize size x size matrix with 0s
		val matrix = Array.fill(size, size)(0)
		for {
			(from, to) <- graph.getEdges.toSeq.sorted
		} {
			matrix(from)(to) = 1
			graph match {
				case _: SimpleMapGraph[_] => matrix(to)(from) = 1
				case _ => ()
			}
		}
		matrix.map(_.toJSArray).toJSArray
	}

	@JSExport
	def getMatrixHoverCell(): js.Array[Int] = matrixHoverCell
			.map { case (col, row) => js.Array.apply(col, row) }
			.getOrElse(new js.Array)

	// TODO move a lot of the logic here to its own file outside of GraphController (e.g., EdgeRenderer)
	@JSExport
	def renderMainCanvas(): Unit = {
		val (lines, arrows) = graph.uniqueEdgesWithDirection.toSeq
			.map { case ((from, to), isBidirectional) =>
				val line = getEdgeCoordinates(from, to).get
				val (canvasLine, arrowTriangles) = graph match {
					case _: SimpleMapGraph[Int] =>
						val canvasLine = EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, simpleEdgeStrokeColor)
						(canvasLine, Seq.empty)
					case _: DirectedMapGraph[Int] =>
						EdgeRender.directedEdge(
							e = line,
							lineWidth = simpleEdgeStrokeWidth,
							lineColor = simpleEdgeStrokeColor,
							shortenFromSrc = isBidirectional,
							shortenFromDest = true,
							shortenAmount = 47.0,
							srcToDestArrow = Some(ArrowRenderProperties.default),
							destToSrcArrow = if (isBidirectional) Some(ArrowRenderProperties.default) else None
						)
				}
				(canvasLine, arrowTriangles)
			}.foldLeft((Seq.empty[CanvasLine], Seq.empty[TriangleCanvas])) {
				case ((linesAcc, trianglesAcc), (line, triangles)) =>
					(linesAcc :+ line, trianglesAcc ++ triangles)
			}

		// Get shapes for highlighted edge (which will get drawn on top of existing edge)
		// TODO: this is very similar but not identical to the code above. Refactor to avoid duplication?
		val highlightedEdge = hoveredEdge match {
			case Some((from, to)) =>
				val line = getEdgeCoordinates(from, to).get
				val isBidirectional = graph.hasEdge(to, from)
				graph match {
					case _: SimpleMapGraph[Int] =>
						Seq(EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, edgeHighlightColor))
					case _: DirectedMapGraph[Int] =>
						val (highlightedLine, highlightedArrows) = EdgeRender.directedEdge(
							e = line,
							lineWidth = simpleEdgeStrokeWidth,
							lineColor = edgeHighlightColor,
							shortenFromSrc = isBidirectional,
							shortenFromDest = true,
							shortenAmount = 47.0,
							srcToDestArrow = Some(ArrowRenderProperties.default.copy(color = edgeHighlightColor)),
							destToSrcArrow = None
						)
						Seq(highlightedLine) ++ highlightedArrows
				}
			case None =>
				Seq.empty[RenderOp]
		}

		// get potential edge shape
		val potentialEdgeOpt: Option[Seq[RenderOp]] = matrixHoverCell.flatMap { case (from, to) =>
			if (!graph.hasEdge(from, to) && from != to) { // disallow self-loops
				getEdgeCoordinates(from, to).map { line =>
					graph match {
						case _: SimpleMapGraph[Int] =>
							Seq(EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, "rgba(0, 0, 255, 0.5)")) // semi-transparent blue
						case _: DirectedMapGraph[Int] =>
							val (canvasLine, arrowTriangles) = EdgeRender.directedEdge(
								e = line,
								lineWidth = simpleEdgeStrokeWidth,
								lineColor = potentialEdgeStrokeColor, // semi-transparent blue
								shortenFromSrc = false,
								shortenFromDest = true,
								shortenAmount = 47.0,
								srcToDestArrow = Some(ArrowRenderProperties.default.copy(color = potentialArrowColor)),
								destToSrcArrow = None
							)
							Seq(canvasLine) ++ arrowTriangles
					}
				}
			} else None
		}

		// we want to draw the shapes in the correct order, with arrows on top of lines, etc.
		val orderedShapes =  lines ++ arrows ++ highlightedEdge ++ potentialEdgeOpt.toSeq.flatten
		MainCanvas.setShapes(orderedShapes) // draw arrows after lines so they appear on top
	}

	@JSExport
	def getFullNodeData(): js.Array[KeyWithData] = {
		keyToData
			.map { case (key, data) => KeyWithDataConverter.toJS(key, data) }
			.toJSArray
	}

	@JSExport
	def getNodeData(key: Int): NodeDataJS = keyToData(key).toJS

	/** First node will be labeled '0', next will be labeled '1', etc. */
	@JSExport
	def nextNodeKey(): Int = graph.nodeCount

	@JSExport
	def containsEdge(from: Int, to: Int): Boolean = graph.hasEdge(from, to)

	@JSExport
	def toggleDirectionality(): Unit = {
		graph match {
			case g: DirectedMapGraph[Int] =>
				var undirectedGraph = new SimpleMapGraph[Int]()
				// add all nodes
				for (node <- g.adjMap.keys) {
					undirectedGraph = undirectedGraph.addNode(node)
				}
				// add all edges in undirected manner
				for {
					(from, neighbors) <- g.adjMap
					to <- neighbors
				} {
					undirectedGraph = undirectedGraph.addEdge(from, to)
				}
				graph = undirectedGraph
			case g: SimpleMapGraph[Int] =>
				graph = new DirectedMapGraph[Int](g.adjMap)
		}
	}

	@JSExport
	def hoverAdjMatrixCell(col: Int, row: Int): Unit = {
		// the mouseover listener can sometimes report negative coordinates if you move the mouse fast enough, so check it's not negative
		def withinBounds(x: Int) = { x >= 0 && x < graph.nodeCount }
		if (withinBounds(col) && withinBounds(row)) {
			matrixHoverCell = Some((col, row))
		} else matrixHoverCell = None
	}

	@JSExport
	def leaveAdjMatrix(): Unit = { matrixHoverCell = None }

	@JSExport
	def adjMatrixClick(): Unit = {
		println("Adjacency matrix cell clicked at " + matrixHoverCell)
	}

	@JSExport
	def removeEdge(from: Int, to: Int): Unit = {
		try {
			graph = graph.removeEdge(from, to)
		} catch {
			case e: NoSuchElementException => println(s"Error removing edge: ${e.getMessage}")
		}
	}
}
