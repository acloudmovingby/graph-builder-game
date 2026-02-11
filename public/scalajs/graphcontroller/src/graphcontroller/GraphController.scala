package graphcontroller

import graphcontroller.components.maincanvas.{ArrowRenderProperties, EdgeRender, MainCanvas}

import scala.scalajs.js
import js.JSConverters.*
import scala.scalajs.js.annotation.*
import graphi.{DirectedMapGraph, MapGraph, SimpleMapGraph}
import graphcontroller.dataobject.{KeyWithData, KeyWithDataConverter, Line, NodeData, NodeDataJS, Vector2D}
import graphcontroller.dataobject.canvas.{CanvasLine, CanvasRenderOp, TriangleCanvas}
import EdgeRender.{edgeHighlightColor, potentialArrowColor, potentialEdgeStrokeColor, simpleEdgeStrokeColor, simpleEdgeStrokeWidth}
import graphcontroller.model.GraphUndoState
import graphcontroller.controller.Controller

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * The original, now deprecated, way of using ScalaJS. It was called from by the vanilla JS code when I first
 * started doing some of the graph logic with Scala (since that's obviously far superior than vanilla JS). This is why
 * there is all these JSExport annotations. It's also a somewhat disorganized bag of stuff that I'm trying to slowly
 * move over to the new way of doing things.
 * */
@JSExportTopLevel("GraphController")
class GraphController {
	private def state = Controller.state
	private def keyToData = state.keyToData
	private var matrixHoverCell: Option[(Int, Int)] = None
	private def hoveredEdge: Option[(Int, Int)] = matrixHoverCell.flatMap { case (from, to) =>
		if (state.graph.hasEdge(from, to)) Some((from, to)) else None
	}

	@JSExport
	def isDirected(): Boolean = state.graph match {
		case _: DirectedMapGraph[Int] => true
		case _: SimpleMapGraph[Int] => false
	}

	@JSExport
	def clearGraph(): Unit = {
		Controller.state = state.copy(graph = new DirectedMapGraph[Int]())
		Controller.state = state.copy(keyToData = Map.empty)
	}

	@JSExport
	def nodeCount(): Int = state.graph.nodeCount

	@JSExport
	def edgeCount(): Int = state.graph.edgeCount

	@JSExport
	def addNode(key: Int, data: NodeDataJS): Unit = {
		Controller.state = state.copy(graph = state.graph.addNode(key))
		Controller.state = state.copy(keyToData = keyToData + (key -> NodeData.fromJS(data)))
	}

	@JSExport
	def addEdge(to: Int, from: Int): Unit = try {
		Controller.state = state.copy(graph = state.graph.addEdge(to, from))
	} catch {
		case e: NoSuchElementException => println(s"Error adding edge (not yet implemented): ${e.getMessage}")
	}

	@JSExport
	def updateNodeData(key: Int, data: NodeDataJS): Unit = {
		keyToData.get(key) match {
			case Some(_) => Controller.state = state.copy(keyToData = keyToData + (key -> NodeData.fromJS(data)))
			case None => println(s"Error updating node data: Node $key does not exist")
		}
	}

	@JSExport
	def pushUndoState(): Unit = {
		// TODO get rid of this clone thing, it doesn't make sense
		val nodeCopyFunction = (i: Int) => i // identity function for Int keys
		val newUndoState = GraphUndoState(state.graph.clone(nodeCopyFunction), keyToData)
		Controller.state = state.copy(undoStack = newUndoState :: Controller.state.undoStack)
		// if we exceed the limit, remove the oldest state
		if (Controller.state.undoStack.size > GraphUndoState.UNDO_SIZE_LIMIT) {
			val newStack = Controller.state.undoStack.take(GraphUndoState.UNDO_SIZE_LIMIT)
			Controller.state = Controller.state.copy(undoStack = newStack)
		}
	}

	/**	No-op if no states in undo stack */
	@JSExport
	def popUndoState(): Unit = {
		// if it's non-empty, pop the top state and restore it
		Controller.state.undoStack.headOption.foreach { prevState =>
			Controller.state = Controller.state.copy(undoStack = Controller.state.undoStack.tail)
			Controller.state = state.copy(graph = prevState.graph)
			Controller.state = state.copy(keyToData = prevState.keyToData)
		}
	}

	/** For graying-out the undo button if can't undo anymore */
	@JSExport
	def canUndo(): Boolean = Controller.state.undoStack.nonEmpty

	@JSExport
	def getAdjList(): js.Array[js.Array[Int]] = state.graph.adjMap.map(_._2.toSeq.toJSArray).toJSArray

	private def getEdgeCoordinates(fromIndex: Int, toIndex: Int): Option[Line] = for {
		fromData <- keyToData.get(fromIndex)
		toData <- keyToData.get(toIndex)
	} yield Line(
		from = Vector2D(fromData.x, fromData.y),
		to = Vector2D(toData.x, toData.y)
	)

	private def getEdgeObjects(g: MapGraph[Int]): Seq[Line] =
		g.getEdges.toSeq.flatMap { case (from, to) => getEdgeCoordinates(from, to).toSeq }

	// TODO move a lot of the logic here to its own file outside of GraphController (e.g., EdgeRenderer)
	@JSExport
	def renderMainCanvas(): Unit = {
		val lineBuffer = ListBuffer.empty[CanvasLine]
		val arrowBuffer = ListBuffer.empty[TriangleCanvas]

		state.graph.uniqueEdgesWithDirection.toSeq
			.foreach { case ((from, to), isBidirectional) =>
				val line = getEdgeCoordinates(from, to).get
				val (canvasLine, arrowTriangles) = state.graph match {
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
				canvasLine +=: lineBuffer
				arrowTriangles ++=: arrowBuffer
			}

		val (lines, arrows) = (lineBuffer.toSeq, arrowBuffer.toSeq)

		// Get shapes for highlighted edge (which will get drawn on top of existing edge)
		// TODO: this is very similar but not identical to the code above. Refactor to avoid duplication?
		val highlightedEdge = hoveredEdge match {
			case Some((from, to)) =>
				val line = getEdgeCoordinates(from, to).get
				val isBidirectional = state.graph.hasEdge(to, from)
				state.graph match {
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
				Seq.empty[CanvasRenderOp]
		}

		// get potential edge shape
		val potentialEdgeOpt: Option[Seq[CanvasRenderOp]] = matrixHoverCell.flatMap { case (from, to) =>
			if (!state.graph.hasEdge(from, to) && from != to) { // disallow self-loops
				getEdgeCoordinates(from, to).map { line =>
					state.graph match {
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
	def nextNodeKey(): Int = state.graph.nodeCount

	@JSExport
	def containsEdge(from: Int, to: Int): Boolean = state.graph.hasEdge(from, to)

	@JSExport
	def toggleDirectionality(): Unit = {
		state.graph match {
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
				Controller.state = state.copy(graph = undirectedGraph)
			case g: SimpleMapGraph[Int] =>
				Controller.state = state.copy(graph = new DirectedMapGraph[Int](g.adjMap))
		}
	}

	@JSExport
	def removeEdge(from: Int, to: Int): Unit = {
		try {
			Controller.state = state.copy(graph = state.graph.removeEdge(from, to))
		} catch {
			case e: NoSuchElementException => println(s"Error removing edge: ${e.getMessage}")
		}
	}
}
