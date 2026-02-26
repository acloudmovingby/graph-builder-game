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
}
