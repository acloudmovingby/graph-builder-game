package graphcontroller.view.maincanvas

import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.{CanvasLine, RectangleCanvas, RenderOp}
import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.*
import graphcontroller.view.AdjacencyMatrixViewData
import graphcontroller.render.EdgeRender
import graphcontroller.render.properties.ArrowRenderProperties
import graphi.{DirectedMapGraph, MapGraph, SimpleMapGraph}

object MainCanvasView {

	/*
	// it's getting the case where a cell is being hovered over that represents an edge
	val highlightedEdge = hoveredEdge match {
			// if the edge exists, render it as highlighted
			case Some((from, to)) =>
				// get edge coordinates
				val line = getEdgeCoordinates(from, to).get
				// decide if isBidirectional but that only matters for directed
				val isBidirectional = state.graph.hasEdge(to, from)
				state.graph match {
					case _: SimpleMapGraph[Int] =>
						Seq(EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, edgeHighlightColor))
					case _: DirectedMapGraph[Int] =>
						val (highlightedLine, highlightedArrows) = EdgeRender.directedEdge(
							e = line,
							lineWidth = simpleEdgeStrokeWidth,
							lineColor = edgeHighlightColor,
							shortenFromSrc = isBidirectional,
							shortenFromDest = true,
							shortenAmount = 47.0,
							srcToDestArrow = Some(ArrowRenderProperties.default.copy(color = edgeHighlightColor)),
							destToSrcArrow = None
						)
						Seq(highlightedLine) ++ highlightedArrows
				}
			case None =>
				Seq.empty[RenderOp]
		}

		// get potential edge shape
		val potentialEdgeOpt: Option[Seq[RenderOp]] = matrixHoverCell.flatMap { case (from, to) =>
			if (!state.graph.hasEdge(from, to) && from != to) { // disallow self-loops
				getEdgeCoordinates(from, to).map { line =>
					state.graph match {
						case _: SimpleMapGraph[Int] =>
							// isAdd
							Seq(EdgeRender.simpleEdge(line, simpleEdgeStrokeWidth, "rgba(0, 0, 255, 0.5)")) // semi-transparent blue
						case _: DirectedMapGraph[Int] =>
							val (canvasLine, arrowTriangles) = EdgeRender.directedEdge(
								e = line,
								lineWidth = simpleEdgeStrokeWidth,
								lineColor = potentialEdgeStrokeColor, // semi-transparent blue
								shortenFromSrc = false,
								shortenFromDest = true,
								shortenAmount = 47.0,
								srcToDestArrow = Some(ArrowRenderProperties.default.copy(color = potentialArrowColor)),
								destToSrcArrow = None
							)
							Seq(canvasLine) ++ arrowTriangles
					}
				}
			} else None
		}
	 */

	/** Ghostly edges that show on the screen while you're hovering over adjacency matrix. */
	private def potentialEdges(state: State): Seq[RenderOp] = {
		// converts Cell class to a (from, to) edge tuple, but disallowing self-loops
		def getEdgeFromCell(cell: Cell): Option[(Int, Int)] = {
			val from = cell.row
			val to = cell.col
			if (from != to) { // disallow self-loops
				Some((from, to))
			} else None
		}

		val edges = state.adjMatrixState match {
			case Hover(cell) =>
				getEdgeFromCell(cell).toSeq
			case Clicked(cell, isAdd) =>
				getEdgeFromCell(cell).toSeq
			case d: DragSelecting =>
				d.selectedCells.flatMap(getEdgeFromCell).toSeq
			case _ => Seq.empty
		}

		val isAdd = edges.headOption.map(e => !state.graph.hasEdge(e._1, e._2)).getOrElse(true)

		def renderDirectedEdge(from: Int, to: Int): Seq[RenderOp] = {
			val isBidirectional = state.graph.hasEdge(to, from)
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

		state.graph match {
			case g: DirectedMapGraph[Int] =>
				edges.flatMap { case (from, to) =>
					renderDirectedEdge(from, to)
				}
			case g: SimpleMapGraph[Int] => Seq.empty
		}
	}

	def render(state: State): Seq[RenderOp] = {
		potentialEdges(state)
	}
}