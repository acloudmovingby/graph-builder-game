package scalagraph

import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation._
import graphi.DirectedMapGraph
import scalagraph.{CanvasLine, Point, PointJS, CanvasLineJS, EdgeRender}

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

	/**
	 * For now, this passes an array of 4-element arrays, comprised of: [from.x, from.y, to.x, to.y]
	 * Later I may convert more of the node rendering logic as ScalaJS but for now this is what it is.
	 * */
	@JSExport
	def getEdgesForRendering(): js.Array[CanvasLineJS] = EdgeRender.getCanvasLines(graph, keyToData).map(_.toJS).toJSArray

	@JSExport
	def getFullNodeData(): js.Array[KeyWithData] = {
		keyToData
			.map { case (key, data) => KeyWithDataConverter.toJS(key, data) }
			.toJSArray
	}
}