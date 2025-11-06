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
	val UNDO_SIZE_LIMIT = 35
}

@JSExportTopLevel("GraphController")
class GraphController {
	private var graph: DirectedMapGraph[Int] | SimpleMapGraph[Int] = new DirectedMapGraph[Int]() // key is Int, data is NodeData
	private var keyToData = Map[Int, NodeData]()
	private val undoGraphStates = scala.collection.mutable.Stack[GraphState[Int]]()

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

	@JSExport
	def popUndoState(): Unit = {
		if (undoGraphStates.nonEmpty) {
			val prevState = undoGraphStates.pop()
			graph = prevState.graph
			keyToData = prevState.keyToData
		} else {
			println("No undo states available")
		}
	}

	@JSExport
	def getDot: String = graph.toDot

	@JSExport
	def getAdjList(): js.Array[js.Array[Int]] = graph.adjMap.map(_._2.toSeq.toJSArray).toJSArray

	private def getEdgeObjects: Seq[Edge] = graph.getEdges.toSeq.flatMap { case (from, to) =>
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
	def getAllShapes(): MultiShapesCanvasJS = graph match {
		case _: SimpleMapGraph[Int] =>
			val edges = getEdgeObjects
			val lines = EdgeRender.getSimpleEdgesForRendering(edges)
			val shapes = MultiShapesCanvas(lines = lines, triangles = Seq.empty)
			shapes.toJS
		case _: DirectedMapGraph[Int] =>
			val edges = getEdgeObjects
			val lines = EdgeRender.getDirectedEdgesForRendering(edges)
			val triangles = ArrowTipRender.getTriangles(edges)
			val shapes = MultiShapesCanvas(lines = lines, triangles = triangles)
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
				undirectedGraph
				graph = undirectedGraph
			case g: SimpleMapGraph[Int] =>
				graph = new DirectedMapGraph[Int](g.adjMap)
		}
	}
}
