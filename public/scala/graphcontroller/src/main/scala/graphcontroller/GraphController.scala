package graphcontroller

import scala.scalajs.js
import js.JSConverters.*
import scala.scalajs.js.annotation.*
import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.render.{ArrowTipRender, EdgeRender}
import graphcontroller.dataobject.{Edge, KeyWithData, KeyWithDataConverter, NodeData, NodeDataJS, Point}
import graphcontroller.dataobject.canvas.{CanvasLineJS, MultiShapesCanvas, MultiShapesCanvasJS, TriangleCanvasJS}

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

	private def getEdgeObjects(g: DirectedMapGraph[Int] | SimpleMapGraph[Int]): Seq[Edge] =
		g.getEdges.toSeq.flatMap { case (from, to) =>
			(for {
				fromData <- keyToData.get(from)
				toData <- keyToData.get(to)
			} yield Edge(
				from = Point(fromData.x, fromData.y),
				to = Point(toData.x, toData.y)
			)).toSeq
		}

	@JSExport
	def getAdjacencyMatrix(): js.Array[js.Array[Int]] = {
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

	@JSExport
	def getAllShapes(): MultiShapesCanvasJS = {
		val edges = getEdgeObjects(graph)
		val (lines, triangles) = graph match {
			case _: SimpleMapGraph[Int] =>
				val lines = EdgeRender.edgeShapes(edges, "simple")
				(lines, Seq.empty)
			case _: DirectedMapGraph[Int] =>
				val lines = EdgeRender.edgeShapes(edges, "directed")
				val triangles = ArrowTipRender.getTriangles(edges)
				(lines, triangles)
		}
		val shapes = MultiShapesCanvas(lines, triangles)
		shapes.toJS
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
	def removeEdge(from: Int, to: Int): Unit = {
		try {
			graph = graph.removeEdge(from, to)
		} catch {
			case e: NoSuchElementException => println(s"Error removing edge: ${e.getMessage}")
		}
	}
}
