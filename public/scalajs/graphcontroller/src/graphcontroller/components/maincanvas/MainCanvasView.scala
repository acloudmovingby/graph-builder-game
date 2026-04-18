package graphcontroller.components.maincanvas

import graphi.{DirectedMapGraph, SimpleMapGraph}
import graphcontroller.components.RenderOp
import graphcontroller.components.adjacencymatrix.{CellClicked, Hover}
import graphcontroller.components.maincanvas.EdgeRender.{simpleEdgeStrokeColor, simpleEdgeStrokeWidth}
import graphcontroller.components.maincanvas.NodeRenderStyle.{AddEdgeHover, AddEdgeHoverStart, AddEdgeNotStart, AddEdgeStart, Basic, BasicHover, Selected}
import graphcontroller.dataobject.canvas.{Border, CanvasLine, CanvasPolyLine, CanvasRenderOp, CircleCanvas, RectangleCanvas, ShapeStyle, TriangleCanvas}
import graphcontroller.dataobject.{Cell, Circle, Column, Line, NodeData, Rectangle, Row, Vector2D}
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{AreaCompleteTool, BuildTool, MagicPathTool, MoveTool, SelectMode, SelectTool, Tool}
import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable.ListBuffer

object MainCanvasView {
	/** Ghostly edges that show on the screen while you're hovering over adjacency matrix. */
	private def potentialEdges(state: State): Seq[CanvasRenderOp] = {
		val graph = state.graph

		// converts Cell class to a (from, to) edge tuple, but disallowing self-loops
		def getEdgeFromCell(cell: Cell): Option[(Int, Int)] = {
			if (cell.col == cell.row) None // don't show self-edges (as of now)
			else Some(state.cellToNodeTuple(cell))
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
	 * For each node, decide what styling it should be rendered with. Return as list of tuples
	 * 
	 * TODO: write unit tests for this (which is easier/better
	 * than writing unit tests for the final thing that produces the CanvasRenderOp because maybe we will change how each
	 * style is rendered and the NodeRenderStyle is a simple enum)
	 */
	def nodesWithStyles(nodes: Seq[Int], hoveringOnNode: Option[HoveredNode], toolState: Tool, selectedNodes: Set[Int] = Set.empty): Seq[(Int, NodeRenderStyle)] = {
		val (nonHoveredNodes, hoveredNode, justAdded) = hoveringOnNode match {
			case None => (nodes, None, false)
			case Some(HoveredNode(nodeIndex, justAdded)) =>
				(nodes.filter(_ != nodeIndex), Some(nodeIndex), justAdded)
		}

		def default = {
			val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = nonHoveredNodes.map(n => (n, Basic))
			val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode.map(n => (n, if (justAdded) Basic else BasicHover))
			nonHoveredStyles ++ hoveredStyle
		}

		val baseStyles = toolState match {
			case _: SelectTool =>
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = nonHoveredNodes.map(n => (n, Basic))
				val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode.map(n => (n, Selected))
				nonHoveredStyles ++ hoveredStyle
			case BuildTool(Some(edgeStart)) => // in edge adding mode
				val withoutEdgeStart = nonHoveredNodes.filter(_ != edgeStart)
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = withoutEdgeStart.map(n => (n, AddEdgeNotStart))
				val edgeStartStyle: Option[(Int, NodeRenderStyle)] = Some((edgeStart, AddEdgeStart))
				val hoveredStyle: Option[(Int, NodeRenderStyle)] =
					hoveredNode
						.filter(_ != edgeStart) // ignore if it's the start node, since we already created the style for that above
						.map(n => (n, AddEdgeHover))
				nonHoveredStyles ++ edgeStartStyle ++ hoveredStyle
			case MagicPathTool(Some(edgeStart)) => // in edge adding mode
				// also almost the same as BasicPath(Some(...))
				val withoutEdgeStart = nonHoveredNodes.filter(_ != edgeStart)
				val nonHoveredStyles: Seq[(Int, NodeRenderStyle)] = withoutEdgeStart.map(n => (n, AddEdgeNotStart))
				val edgeStartStyle: Option[(Int, NodeRenderStyle)] = Some((edgeStart, AddEdgeStart))
				// this part is different since hovering over a node actually causes an edge to be added
				val hoveredStyle: Option[(Int, NodeRenderStyle)] = hoveredNode match {
					case Some(n) if n == edgeStart => Some((n, AddEdgeHoverStart)) // slightly different than BuildTool
					case _ => None
				}
				nonHoveredStyles ++ edgeStartStyle ++ hoveredStyle
			case _ => default
		}

		if (selectedNodes.isEmpty) baseStyles
		else baseStyles.map {
			case (node, _) if selectedNodes.contains(node) => (node, Selected)
			case other => other
		}
	}

	def nodes(state: State): Seq[CanvasRenderOp] = {
		nodesWithStyles(state.graph.nodes, state.canvasInteraction.hoveredNode, state.toolState, state.selectedNodes)
			.flatMap { case (node, style) =>
				state.keyToData.get(node) match {
					case None =>
						Seq.empty
					case Some(data) =>
						val label = if (state.labelsVisible) Some(node.toString) else None
						NodeRender.createNodeCanvasObject(Vector2D(data.x, data.y), label, style)
				}
			}
	}

	private def edgeAddingIndicatorLine(state: State): Option[CanvasLine] = {
		val maybeEdgeStart = state.toolState match {
			case BuildTool(maybeEdgeStart) => maybeEdgeStart
			case MagicPathTool(maybeEdgeStart) => maybeEdgeStart
			case _ => None
		}
		for {
			edgeStart <- maybeEdgeStart
			data <- state.keyToData.get(edgeStart)
		} yield {
			EdgeRender.edgeAddingIndicatorLine(Vector2D(data.x, data.y), state.canvasInteraction.lastMousePosition)
		}
	}

	/** The dotted circle at the cursor when doing the magic path tool. Probably could make into svg icon for cursor
	 * instead of this */
	def magicPathTargetCircle(state: State): Option[CircleCanvas] = {
		state.toolState match {
			case MagicPathTool(Some(_)) =>
				val cc = CircleCanvas(
					Circle(state.canvasInteraction.lastMousePosition, 30),
					style = ShapeStyle(border = Some(Border("black", 2, Seq(5, 5))))
				)
				Some(cc)
			case _ => None
		}
	}

	def areaComplete(state: State): Option[CanvasPolyLine] = {
		state.toolState match {
			case AreaCompleteTool(true, points) =>
				Some(CanvasPolyLine(
					points = points.reverse, // if you don't reverse, it makes the dotted line move in a weird way as you draw since the dash pattern starts at the first point
					style = ShapeStyle(
						fillColor = Some("rgba(255, 130, 172, 0.15)"), // a bit transparent
						border = Some(Border(
							color = "red",
							width = 1.5,
							lineDashSegments = Seq(5, 5)
						))
					)
				))
			case _ => None
		}
	}

	private def getEdgeCoordinates(fromIndex: Int, toIndex: Int, keyToData: Map[Int, NodeData]): Option[Line] = for {
		fromData <- keyToData.get(fromIndex)
		toData <- keyToData.get(toIndex)
	} yield Line(
		from = Vector2D(fromData.x, fromData.y),
		to = Vector2D(toData.x, toData.y)
	)

	/** THe basic edge drawing (directed or undirected) but no highlighted edges  */
	def edges(state: State): Seq[CanvasRenderOp] = {
		val lineBuffer = ListBuffer.empty[CanvasLine]
		val arrowBuffer = ListBuffer.empty[TriangleCanvas]

		state.graph.uniqueEdgesWithDirection.toSeq
			.foreach { case ((from, to), isBidirectional) =>
				val line = getEdgeCoordinates(from, to, state.keyToData).get
				val (canvasLine, arrowTriangles) = state.graph match {
					case _: SimpleMapGraph[Int] =>
						val canvasLine = EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, simpleEdgeStrokeColor)
						(canvasLine, Seq.empty)
					case _: DirectedMapGraph[Int] =>
						EdgeRender.directedEdge(
							e = line,
							lineWidth = simpleEdgeStrokeWidth,
							lineColor = simpleEdgeStrokeColor,
							shortenFromSrc = isBidirectional,
							shortenFromDest = true,
							shortenAmount = 47.0,
							srcToDestArrow = Some(ArrowRenderProperties.default),
							destToSrcArrow = if (isBidirectional) Some(ArrowRenderProperties.default) else None
						)
				}
				canvasLine +=: lineBuffer
				arrowTriangles ++=: arrowBuffer
			}

		val (lines, arrows) = (lineBuffer.toSeq, arrowBuffer.toSeq)

		// we want to draw the shapes in the correct order, e.g. with arrows on top of lines
		lines ++ arrows
	}
	
	/** A dashed bounding rectangle around all selected nodes, drawn when 2+ nodes are selected.
	 *  Separate from the drag-selection box — this one persists after the mouse is released. */
	def selectionBoundingBox(selectedNodes: Set[Int], keyToData: Map[Int, NodeData]): Seq[CanvasRenderOp] = {
		if (selectedNodes.size < 2) return Seq.empty
		val selectedData = selectedNodes.flatMap(keyToData.get)
		if (selectedData.isEmpty) return Seq.empty

		val padding = NodeRender.baseNodeRadius + 10
		val minX = selectedData.map(_.x).min - padding
		val minY = selectedData.map(_.y).min - padding
		val maxX = selectedData.map(_.x).max + padding
		val maxY = selectedData.map(_.y).max + padding

		val rect = Rectangle(Vector2D(minX, minY), maxX - minX, maxY - minY)
		Seq(RectangleCanvas(
			rect,
			style = ShapeStyle(
				fillColor = Some("rgba(0, 0, 0, 0)"),
				border = Some(Border(NodeRender.color1, 1.5, Seq(6, 4)))
			)
		))
	}

	def selectionBox(toolState: Tool, lastMousePosition: Vector2D): Seq[CanvasRenderOp] = {
		toolState match {
			case SelectTool(SelectMode.DraggingBox(start, _)) =>
				val width = lastMousePosition.x - start.x
				val height = lastMousePosition.y - start.y
				val rect = Rectangle(start, width, height)
				val alpha = "33" // transparency (alpha) to append to the hex color
				Seq(RectangleCanvas(rect, style = ShapeStyle.filledAndStroked(NodeRender.color1 + alpha, NodeRender.color1, 1.0)))
			case _ => Seq.empty
		}
	}


	def render(state: State): MainCanvasViewData = {
		// This is all the shapes drawn to canvas, and is essentially the Z-order of the canvas
		val ops = edges(state) ++
			potentialEdges(state) ++
			edgeAddingIndicatorLine(state) ++
			selectionBoundingBox(state.selectedNodes, state.keyToData) ++
			nodes(state) ++
			areaComplete(state) ++
			selectionBox(state.toolState, state.canvasInteraction.lastMousePosition)

		MainCanvasViewData(
			ops,
			magicPathTargetCircle(state),
			state.toolState,
			state.graph.nodeCount == 0
		)
	}
}

case class MainCanvasViewData(
	shapes: Seq[CanvasRenderOp],
	magicPathCircle: Option[CircleCanvas],
	currentTool: Tool,
	welcomeMessageVisible: Boolean
) extends RenderOp {
	private def welcomeMessage(): Unit = {
		val welcome = dom.document.getElementById("welcome-message").asInstanceOf[html.Paragraph]
		welcome.style.visibility = if (welcomeMessageVisible) "visible" else "hidden"
	}

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
		welcomeMessage()
	}
}