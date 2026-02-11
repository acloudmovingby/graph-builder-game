package graphcontroller.components.maincanvas

import graphcontroller.dataobject.canvas.{CanvasLine, TriangleCanvas}
import graphcontroller.dataobject.{Line, Vector2D}
import ArrowTipRender.getArrowTriangle

import scala.math

enum EdgeStyle {
	case Simple, Directed, SimpleHighlighted, DirectedHighlighted
}

case class DirectedEdge(e: Line, isBidirectional: Boolean)

object EdgeRender {
	// Parameters for styling the rendered shapes
	val simpleEdgeStrokeColor: String = "orange"
	val edgeHighlightColor: String = "#F2813B"
	val simpleEdgeStrokeWidth: Int = 8
	val potentialEdgeStrokeColor: String = "rgba(255, 165, 0, 0.5)" // orange with 50% opacity
	val potentialArrowColor: String = "rgba(50, 191, 227, 0.5)" // blue arrow head with 50% opacity

	case class DirectedEdge(bidirectional: Boolean, edge: Line)

	private def decideDirectionality(edges: Seq[Line]): List[DirectedEdge] = {
		val edgeSet = edges.map(e => (e.from, e.to)).toSet
		var seen = Set.empty[(Vector2D, Vector2D)]
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
		edge: Line,
		displacement: Double = 47.0
	): Line = {
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
				Line(Vector2D(from.x + dxFromNode, from.y + dyFromNode), to)
			} else {
				Line(from, Vector2D(to.x - dxFromNode, to.y - dyFromNode))
			}
		}
	}

	def trimEdgesBasedOnDirectionality(
		directedEdges: Seq[DirectedEdge],
		displacement: Double = 47.0 // how much to cut off from end
	): Seq[Line] = {
		directedEdges.map { de =>
			val trimmedEdge = if (de.bidirectional) trimEdge(trimStart = true, de.edge, displacement) else de.edge
			trimEdge(trimStart = false, trimmedEdge, displacement)
		}
	}

	def getDirectedEdgesForRendering(edges: Seq[Line]): Seq[CanvasLine] = {
		val dirEdges = decideDirectionality(edges)
		val trimmed = trimEdgesBasedOnDirectionality(dirEdges)
		trimmed.map(e => CanvasLine(e.from, e.to, simpleEdgeStrokeWidth, simpleEdgeStrokeColor))
	}

	def getSimpleEdgesForRendering(edges: Seq[Line]): Seq[CanvasLine] = {
		edges.map(e => CanvasLine(e.from, e.to, simpleEdgeStrokeWidth, simpleEdgeStrokeColor))
	}

	def simpleEdge(e: Line, strokeWidth: Int, color: String): CanvasLine = {
		CanvasLine(e.from, e.to, strokeWidth, color)
	}

	/** Takes all the information needed to render the directed edge. Returns tuple of (line, arrows) */
	def directedEdge(
		e: Line,
		lineWidth: Int,
		lineColor: String,
		shortenFromSrc: Boolean,
		shortenFromDest: Boolean,
		shortenAmount: Double,
		srcToDestArrow: Option[ArrowRenderProperties],
		destToSrcArrow: Option[ArrowRenderProperties]
	): (CanvasLine, Seq[TriangleCanvas]) = {
		// shorten the edge from the source and/or destination as needed
		var shortenedEdge = e
		shortenedEdge = if (!shortenFromSrc) shortenedEdge else trimEdge(trimStart = true, shortenedEdge, shortenAmount)
		shortenedEdge = if (!shortenFromDest) shortenedEdge else trimEdge(trimStart = false, shortenedEdge, shortenAmount)
		// create the line
		val line = CanvasLine(shortenedEdge.from, shortenedEdge.to, lineWidth, lineColor)

		// create the arrows as needed
		val s2dArrow = srcToDestArrow.map { props => getArrowTriangle(e, props) }
		val d2sArrow = destToSrcArrow.map { props => getArrowTriangle(Line(e.to, e.from), props) } // reverse the line for arrow pointing back to source

		(line, s2dArrow.toSeq ++ d2sArrow.toSeq)
	}
}
