package graphcontroller.components.maincanvas

import graphcontroller.components.RenderOp
import graphcontroller.components.adjacencymatrix.{CellClicked, Hover}
import graphcontroller.components.maincanvas.NodeRenderStyle.Basic
import graphcontroller.dataobject.canvas.{CanvasRenderOp, CircleCanvas}
import graphcontroller.dataobject.{Cell, Column, Row, Vector2D}
import graphcontroller.model.State
import graphi.{DirectedMapGraph, SimpleMapGraph}

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

	def render(state: State): MainCanvasViewData = {
		MainCanvasViewData(potentialEdges(state))
	}
}

case class MainCanvasViewData(
	shapes: Seq[CanvasRenderOp]
) extends RenderOp {
	def render(): Unit = {
		MainCanvas.setShapesNew(shapes)
	}
}