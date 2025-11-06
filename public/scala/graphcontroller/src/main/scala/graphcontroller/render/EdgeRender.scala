package graphcontroller.render

import scala.math
import graphi.DirectedMapGraph

import graphcontroller.dataobject.{Edge, NodeData, Point}
import graphcontroller.dataobject.canvas.{CanvasLine, TriangleCanvas}

object EdgeRender {
	// Parameters for styling the rendered shapes
	val simpleEdgeStrokeColor: String = "orange"
	val simpleEdgeStrokeWidth: Int = 8

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

	def trimEdge(
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
			val trimmedEdge = if (de.bidirectional) trimEdge(trimStart = true, de.edge, displacement) else de.edge
			trimEdge(trimStart = false, trimmedEdge, displacement)
		}
	}

	def getDirectedEdgesForRendering(edges: Seq[Edge]): Seq[CanvasLine] = {
		val dirEdges = decideDirectionality(edges)
		val trimmed = trimEdgesBasedOnDirectionality(dirEdges)
		trimmed.map(e => CanvasLine(e.from, e.to, simpleEdgeStrokeWidth, simpleEdgeStrokeColor))
	}

	def getSimpleEdgesForRendering(edges: Seq[Edge]): Seq[CanvasLine] = {
		edges.map(e => CanvasLine(e.from, e.to, simpleEdgeStrokeWidth, simpleEdgeStrokeColor))
	}
}
