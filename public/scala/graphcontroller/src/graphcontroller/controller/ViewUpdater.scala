package graphcontroller.controller

import graphcontroller.view.ViewData
import graphcontroller.render.{AdjMatrixCanvas, MainCanvas}

/** Yay! Let's take all the calculated data and actually cause a side effect and render it to the screen! */
object ViewUpdater {
	def updateView(viewData: ViewData): Unit = {
		AdjMatrixCanvas.setShapes(viewData.adjMatrix.shapes)
		MainCanvas.setShapesNew(viewData.mainCanvas)
	}
}