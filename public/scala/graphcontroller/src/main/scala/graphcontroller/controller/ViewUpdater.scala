package graphcontroller.controller

import graphcontroller.view.ViewData
import graphcontroller.render.{AdjMatrixCanvas, MainCanvas}

object ViewUpdater {
	def updateView(viewData: ViewData): Unit = {
		AdjMatrixCanvas.setShapes(viewData.adjMatrix.shapes)
		MainCanvas.setShapesNew(viewData.mainCanvas)
	}
}