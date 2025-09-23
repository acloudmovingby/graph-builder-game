package scalagraph.render

import scala.math
import graphi.DirectedMapGraph

import scalagraph.dataobject.{Edge, NodeData, Point}
import scalagraph.dataobject.canvas.{CanvasLine, TriangleCanvas}

object EdgeRender {
	// Parameters for styling the rendered shapes
	val simpleEdgeStrokeColor: String = "orange"
	val simpleEdgeStrokeWidth: Int = 8

	/**
	 * Given a DirectedMapGraph and a keyToData map, returns a List of CanvasLine representing the edges.
	 * Each CanvasLine contains the start and end Point, width, and color.
	 *
	 * @param graph     The directed graph
	 * @param keyToData Map from node key to NodeData (which contains x, y)
	 * @param directed  If true, treat as directed, else as undirected
	 * @return List of CanvasLine
	 */
	def getCanvasLines(
		graph: DirectedMapGraph[Int],
		keyToData: Map[Int, NodeData],
		directed: Boolean = true,
		color: String = simpleEdgeStrokeColor,
		width: Int = simpleEdgeStrokeWidth
	): List[CanvasLine] = {
		val edges = graph.getEdges
		edges.flatMap { case (from, to) =>
			for {
				fromData <- keyToData.get(from)
				toData <- keyToData.get(to)
			} yield CanvasLine(
				from = Point(fromData.x, fromData.y),
				to = Point(toData.x, toData.y),
				width = width,
				color = color
			)
		}
	}.toList

	case class DirectedEdge(bidirectional: Boolean, edge: Edge)

	def decideDirectionality(edges: Seq[Edge]): List[DirectedEdge] = {
		val edgeSet = edges.map(e => (e.from, e.to)).toSet
		var seen = Set.empty[(Point, Point)]
		val directedEdges = scala.collection.mutable.ListBuffer[DirectedEdge]()
		for (e <- edges) {
			val key = (e.from, e.to)
			val reverseKey = (e.to, e.from)
			if (!seen.contains(key) && !seen.contains(reverseKey)) {
				val bidirectional = edgeSet.contains(reverseKey)
				directedEdges += DirectedEdge(bidirectional, e)
				seen += key
				seen += reverseKey
			}
		}
		directedEdges.toList
	}

	def trimEdge2(
		trimStart: Boolean,
		edge: Edge,
		displacement: Double = 47.0
	): Edge = {
		val (from, to) = (edge.from, edge.to)
		val dx = to.x - from.x
		val dy = to.y - from.y
		val edgeLength = math.sqrt(dx * dx + dy * dy)
		if (edgeLength == 0) edge
		else {
			val ratio = (displacement - 1) / edgeLength
			val dxFromNode = (dx * ratio).toInt
			val dyFromNode = (dy * ratio).toInt
			if (trimStart) {
				Edge(Point(from.x + dxFromNode, from.y + dyFromNode), to)
			} else {
				Edge(from, Point(to.x - dxFromNode, to.y - dyFromNode))
			}
		}
	}

	def trimEdgesBasedOnDirectionality(
		directedEdges: Seq[DirectedEdge],
		displacement: Double = 47.0 // how much to cut off from end
	): Seq[Edge] = {
		directedEdges.map { de =>
			val trimmedEdge = if (de.bidirectional) trimEdge2(trimStart = true, de.edge, displacement) else de.edge
			trimEdge2(trimStart = false, trimmedEdge, displacement)
		}
	}

	def getDirectedEdgesForRendering(edges: Seq[Edge]): Seq[CanvasLine] = {
		val dirEdges = decideDirectionality(edges)
		val trimmed = trimEdgesBasedOnDirectionality(dirEdges)
		trimmed.map(e => CanvasLine(e.from, e.to, simpleEdgeStrokeWidth, simpleEdgeStrokeColor))
	}
}
