package scalagraph

import scala.scalajs.js
import js.JSConverters.*
import scala.scalajs.js.annotation.*
import graphi.DirectedMapGraph
import scalagraph.render.{ArrowTipRender, EdgeRender}
import scalagraph.dataobject.{Edge, KeyWithData, KeyWithDataConverter, NodeData, NodeDataConverter, NodeDataJS, Point}
import scalagraph.dataobject.canvas.{CanvasLineJS, MultiShapesCanvas, MultiShapesCanvasJS, TriangleCanvasJS}

case class GraphState[A](graph: DirectedMapGraph[A], keyToData: Map[A, NodeData])

object GraphState {
	// Limit the number of undo states to avoid excessive memory usage
	val UNDO_SIZE_LIMIT = 25
}

@JSExportTopLevel("GraphController")
class GraphController {
	private var graph = new DirectedMapGraph[Int]() // key is Int, data is NodeData
	private var keyToData = Map[Int, NodeData]()
	private var undoGraphStates = scala.collection.mutable.Stack[GraphState[Int]]()

	@JSExport
	def clearGraph(): Unit = graph = new DirectedMapGraph[Int]()

	@JSExport
	def nodeCount(): Int = graph.nodeCount

	@JSExport
	def edgeCount(): Int = graph.edgeCount

	@JSExport
	def addNode(key: Int, data: NodeDataJS): Unit = {
		graph = graph.addNode(key)
		keyToData += (key -> NodeDataConverter.toScala(data))
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
			case Some(_) => keyToData += (key -> NodeDataConverter.toScala(data))
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

	/**
	 * TODO: change this so we use it for simple directed lines once full rendering logic converted to ScalaJS (also rename getCanvasLines in EdgeRender)
	 * The JS code will ultimately just take lines/triangles and will be empty of triangles when rendering non-directed graphs
	 *
	 * For now, this passes an array of 4-element arrays, comprised of: [from.x, from.y, to.x, to.y]
	 * Later I may convert more of the node rendering logic as ScalaJS but for now this is what it is.
	 * */
	@JSExport protected
	def getEdgesForRendering(): js.Array[CanvasLineJS] = EdgeRender.getCanvasLines(graph, keyToData).map(_.toJS).toJSArray

	@JSExport
	def getEdgesForRendering2(): js.Array[CanvasLineJS] =
		EdgeRender.getDirectedEdgesForRendering(getEdgeObjects).map(_.toJS).toJSArray

	/**
	 * Returns data for rendering arrow tips on directed edges (as a list of triangles).
	 * */
	@JSExport
	def getArrowTrianglesForRendering(): js.Array[TriangleCanvasJS] =
		ArrowTipRender.getTriangles(getEdgeObjects).map(_.toJS).toJSArray

	@JSExport
	def getAllShapes(): MultiShapesCanvasJS = {
		val lines = EdgeRender.getDirectedEdgesForRendering(getEdgeObjects)
		val triangles = ArrowTipRender.getTriangles(getEdgeObjects)
		val shapes = MultiShapesCanvas(lines = lines, triangles = triangles)
		shapes.toJS
	}

	@JSExport
	def getFullNodeData(): js.Array[KeyWithData] = {
		keyToData
			.map { case (key, data) => KeyWithDataConverter.toJS(key, data) }
			.toJSArray
	}
}