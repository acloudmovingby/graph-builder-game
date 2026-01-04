package graphcontroller.render

import scala.math
import graphi.DirectedMapGraph
import graphcontroller.dataobject.{Line, NodeData, Point}
import graphcontroller.dataobject.canvas.{CanvasLine, MultiShapesCanvas, RenderOp, TriangleCanvas}
import graphcontroller.render.ArrowTipRender.getArrowTriangle
import graphcontroller.render.EdgeStyle.{Directed, DirectedHighlighted, Simple, SimpleHighlighted}
import graphcontroller.render.properties.ArrowRenderProperties

enum EdgeStyle {
	case Simple, Directed, SimpleHighlighted, DirectedHighlighted
}

// make new pipeline that turns graph edge list into list of DirectedEdges, then further function that turns it into RenderedDirectedEdge
case class DirectedEdge(e: Line, isBidirectional: Boolean)
case class RenderedDirectedEdge(line: CanvasLine, srcArrow: TriangleCanvas, targetArrow: TriangleCanvas) extends RenderOp

object EdgeRender {
	// Parameters for styling the rendered shapes
	val simpleEdgeStrokeColor: String = "orange"
	val edgeHighlightColor: String = "#F2813B"
	val simpleEdgeStrokeWidth: Int = 8

	case class DirectedEdge(bidirectional: Boolean, edge: Line)

	def decideDirectionality(edges: Seq[Line]): List[DirectedEdge] = {
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
				Line(Point(from.x + dxFromNode, from.y + dyFromNode), to)
			} else {
				Line(from, Point(to.x - dxFromNode, to.y - dyFromNode))
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

	def edgeShapes(
		edges: Seq[Line],
		style: EdgeStyle
	): MultiShapesCanvas = {
		val (lines, triangles) = style match {
			case Simple => (getSimpleEdgesForRendering(edges), Seq.empty)
			case Directed =>(getDirectedEdgesForRendering(edges), ArrowTipRender.getTriangles(edges))
			case SimpleHighlighted => (getSimpleEdgesForRendering(edges).map(e => e.copy(color = edgeHighlightColor)), Seq.empty)
			case DirectedHighlighted =>
				val lines = getDirectedEdgesForRendering(edges).map(e => e.copy(color = edgeHighlightColor))
				val triangles = ArrowTipRender.getTriangles(edges).map(t => t.copy(color = edgeHighlightColor))
				(lines, triangles)
		}
		MultiShapesCanvas(lines, triangles)
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
