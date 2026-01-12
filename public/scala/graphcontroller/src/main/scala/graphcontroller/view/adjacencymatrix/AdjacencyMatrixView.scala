package graphcontroller.view.adjacencymatrix

import graphcontroller.model.State
import graphcontroller.model.adjacencymatrix.NoSelection
import graphcontroller.dataobject.Point
import graphcontroller.dataobject.canvas.CanvasLine
import graphcontroller.view.AdjacencyMatrixViewData

object AdjacencyMatrixView {
	def render(state: State): AdjacencyMatrixViewData = {
		val nodeCount = state.graph.nodeCount

		// This is the old JS code that we used to draw grid lines. Now we'll convert it into Scala:
		// - isHovering will come from the state.adjMatrixState (Anything other than NoSelection means hovering over the matrix)
		// - adjMatrix will be represented by nodeCount
		// - width, height, totalWidth, totalHeight will be derived from state.adjMatrixDimensions
		// - ctx will be replaced by creating CanvasLine objects to represent the lines to be drawn
		/*
		if (isHovering) {
			ctx.beginPath();
			for (let i = 1; i < adjMatrix.length; i++) {
				ctx.lineWidth = 1;
				ctx.strokeStyle = "lightgray";
				// vertical lines
				ctx.moveTo(width * i, 0);
				ctx.lineTo(width * i, totalWidth);
				// horizontal lines
				ctx.moveTo(0, height * i);
				ctx.lineTo(totalHeight, height * i);
			}
			ctx.closePath();
			ctx.stroke();
		}
		*/

		if (nodeCount == 0 || state.adjMatrixState == NoSelection) {
			AdjacencyMatrixViewData(lines = Seq())
		} else {
			val totalWidth = state.adjMatrixDimensions._1
			val totalHeight = state.adjMatrixDimensions._2
			val width = totalWidth / nodeCount
			val height = totalHeight / nodeCount

			val lines = for {
				i <- 1 until nodeCount
				verticalLine = CanvasLine(
					from = Point(x = width * i, y = 0),
					to = Point(x = width * i, y = totalHeight),
					width = 1,
					color = "lightgray"
				)
				horizontalLine = CanvasLine(
					from = Point(x = 0, y = height * i),
					to = Point(x = totalWidth, y = height * i),
					width = 1,
					color = "lightgray"
				)
			} yield Seq(verticalLine, horizontalLine)

			AdjacencyMatrixViewData(lines = lines.flatten)
		}
	}
}