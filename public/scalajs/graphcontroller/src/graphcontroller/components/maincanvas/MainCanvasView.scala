package graphcontroller.components.maincanvas

import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.components.RenderOp
import graphcontroller.components.adjacencymatrix.{CellClicked, Hover}
import graphcontroller.components.maincanvas.NodeRenderStyle.{AddEdgeHover, AddEdgeHoverStart, AddEdgeNotStart, AddEdgeStart, Basic, BasicHover}
import graphcontroller.dataobject.canvas.{CanvasLine, CanvasRenderOp, CircleCanvas}
import graphcontroller.dataobject.{Cell, Circle, Column, Row, Vector2D}
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{BasicTool, MagicPathTool, Tool}
import org.scalajs.dom
import org.scalajs.dom.html

object MainCanvasView {
	/** Ghostly edges that show on the screen while you're hovering over adjacency matrix. */
	private def potentialEdges(state: State): Seq[CanvasRenderOp] = {
		val graph = state.graph

		// converts Cell class to a (from, to) edge tuple, but disallowing self-loops
		def getEdgeFromCell(cell: Cell): Option[(Int, Int)] = {
			val from = cell.row
			val to = cell.col
			if (from != to) { // disallow self-loops
				Some((from, to))
			} else None
		}

		val edges = state.adjMatrixState match {
			case Hover(cell: Cell) =>
				getEdgeFromCell(cell).toSeq
			case Hover(row: Row) =>
				row.cells(graph.nodeCount).flatMap(getEdgeFromCell)
			case Hover(col: Column) =>
				col.cells(graph.nodeCount).flatMap(getEdgeFromCell)
			case d: CellClicked =>
				d.selectedCells.flatMap(getEdgeFromCell)
					.filter { e =>
						(d.isAdd, graph.hasEdge(e._1, e._2)) match {
							case (true, false) => true // adding an edge that doesn't exist
							case (false, true) => true // removing an edge that does exist
							case _ => false // otherwise don't change what's drawn
						}
					}
					.toSeq
			case _ => Seq.empty
		}

		val isAdd = edges.headOption.map(e => !graph.hasEdge(e._1, e._2)).getOrElse(true)

		def renderDirectedEdge(from: Int, to: Int): Seq[CanvasRenderOp] = {
			val isBidirectional = graph.hasEdge(to, from)
			state.getEdgeCoordinates(from, to).toSeq.flatMap { line =>
				val (canvasLine, arrowTriangles) = EdgeRender.directedEdge(
					e = line,
					lineWidth = EdgeRender.simpleEdgeStrokeWidth,
					// TODO: rename these colors so they make more sense in this context
					lineColor = if (isAdd) EdgeRender.potentialEdgeStrokeColor else EdgeRender.edgeHighlightColor, // semi-transparent blue
					shortenFromSrc = if (isAdd) false else isBidirectional,
					shortenFromDest = true,
					shortenAmount = 47.0,
					srcToDestArrow = Some(ArrowRenderProperties.default.copy(color = {
						if (isAdd) EdgeRender.potentialArrowColor else EdgeRender.edgeHighlightColor
					})),
					destToSrcArrow = None
				)
				Seq(canvasLine) ++ arrowTriangles
			}
		}

		graph match {
			case g: DirectedMapGraph[Int] =>
				edges.flatMap { case (from, to) =>
					renderDirectedEdge(from, to)
				}
			case g: SimpleMapGraph[Int] => Seq.empty
		}
	}

	/**
	 * TODO don't pass in all of state, just pass in values we need and write unit tests for this (which is easier/better
	 * than writing unit tests for the final thing that produces the CanvasRenderOp because maybe we will change how each
	 * style is rendered and the NodeRenderStyle is a simple enum)
	 */
	def nodesWithStyles(nodes: Seq[Int], hoveringOnNode: Option[HoveredNode], toolState: Tool): Seq[(Int, NodeRenderStyle)] = {
		val (nonHoveredNodes, hoveredNode, justAdded) = hoveringOnNode match {
			case None => (nodes, None, false)
			case Some(HoveredNode(nodeIndex, justAdded)) =>
				(nodes.filter(_ != nodeIndex), Some(nodeIndex), justAdded)
		}

		toolState match {
			case BasicTool(None) =>
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = nonHoveredNodes.map(n => (n, Basic))
				val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode.map(n => (n, if (justAdded) Basic else BasicHover))
				nonHoveredStyles ++ hoveredStyle
			case BasicTool(Some(edgeStart)) => // in edge adding mode
				val withoutEdgeStart = nonHoveredNodes.filter(_ != edgeStart)
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = withoutEdgeStart.map(n => (n, AddEdgeNotStart))
				val edgeStartStyle: Option[(Int, NodeRenderStyle)] = Some((edgeStart, AddEdgeStart))
				val hoveredStyle: Option[(Int, NodeRenderStyle)] =
					hoveredNode
						.filter(_ != edgeStart) // ignore if it's the start node, since we already created the style for that above
						.map(n => (n, AddEdgeHover))
				nonHoveredStyles ++ edgeStartStyle ++ hoveredStyle
			case MagicPathTool(None) =>
				// same as BasicPath(None)
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = nonHoveredNodes.map(n => (n, Basic))
				val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode.map(n => (n, if (justAdded) Basic else BasicHover))
				nonHoveredStyles ++ hoveredStyle
			case MagicPathTool(Some(edgeStart)) => // in edge adding mode
				// also almost the same as BasicPath(Some(...))
				val withoutEdgeStart = nonHoveredNodes.filter(_ != edgeStart)
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = withoutEdgeStart.map(n => (n, AddEdgeNotStart))
				val edgeStartStyle: Option[(Int, NodeRenderStyle)] = Some((edgeStart, AddEdgeStart))
				// this part is different since hovering over a node actually causes an edge to be added
				val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode match {
					case Some(n) if n == edgeStart => Some((n, AddEdgeHoverStart)) // slightly different than BasicTool
					case _ => None
				}
				nonHoveredStyles ++ edgeStartStyle ++ hoveredStyle
			case _ => Seq.empty
		}
	}

	def nodes(state: State): Seq[CanvasRenderOp] = {
		nodesWithStyles(state.graph.nodes, state.hoveringOnNode, state.toolState)
			.flatMap { case (node, style) =>
				val data = state.keyToData(node)
				NodeRender.createNodeCanvasObject(Vector2D(data.x, data.y), Some(node.toString), style)
			}
	}

	private def edgeAddingIndicatorLine(state: State): Option[CanvasLine] = {
		val maybeEdgeStart = state.toolState match {
			case BasicTool(maybeEdgeStart) => maybeEdgeStart
			case MagicPathTool(maybeEdgeStart) => maybeEdgeStart
			case _ => None
		}
		for {
			edgeStart <- maybeEdgeStart
			data <- state.keyToData.get(edgeStart)
		} yield {
			EdgeRender.edgeAddingIndicatorLine(Vector2D(data.x, data.y), state.lastMainCanvasMousePosition)
		}
	}

	/** The dotted circle at the cursor when doing the magic path tool. Probably could make into svg icon for cursor
	 * instead of this */
	def magicPathTargetCircle(state: State): Option[CircleCanvas] = {
		state.toolState match {
			case MagicPathTool(Some(_)) =>
				val cc = CircleCanvas(
					Circle(state.lastMainCanvasMousePosition, 30),
					fillColor = None,
					borderColor = Some("black"),
					borderWidth = Some(2),
					lineDashSegments = Seq(5, 5)
				)
				Some(cc)
			case _ => None
		}
	}

	def render(state: State): MainCanvasViewData = {
		MainCanvasViewData(
			potentialEdges(state) ++ edgeAddingIndicatorLine(state) ++ nodes(state),
			magicPathTargetCircle(state),
			state.toolState
		)
	}
}

case class MainCanvasViewData(
	shapes: Seq[CanvasRenderOp],
	magicPathCircle: Option[CircleCanvas],
	currentTool: Tool
) extends RenderOp {
	def render(): Unit = {
//		val testNodes = Seq(
//			NodeRender.createNodeCanvasObject(Vector2D(100, 100), Some("22"), Basic),
//			NodeRender.createNodeCanvasObject(Vector2D(100, 200), Some("22"), BasicHover),
//			NodeRender.createNodeCanvasObject(Vector2D(100, 300), Some("22"), AddEdgeStart),
//			NodeRender.createNodeCanvasObject(Vector2D(100, 400), Some("22"), AddEdgeNotStart),
//			NodeRender.createNodeCanvasObject(Vector2D(100, 500), Some("22"), AddEdgeHover),
//			NodeRender.createNodeCanvasObject(Vector2D(100, 600), Some("22"), AddEdgeHoverStart)
//		).flatten
		MainCanvas.setShapesNew(shapes ++ magicPathCircle /* ++ testNodes*/)
		if (magicPathCircle.isEmpty) {
			dom.document.getElementById("canvas-area").asInstanceOf[html.Div].style.cursor = currentTool.cursorIconPath
		} else {
			dom.document.getElementById("canvas-area").asInstanceOf[html.Div].style.cursor = "none"
		}
	}
}