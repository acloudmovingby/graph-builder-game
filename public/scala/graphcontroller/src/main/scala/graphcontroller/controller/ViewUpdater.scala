package graphcontroller.controller

import graphcontroller.view.ViewData
import graphcontroller.render.AdjMatrixCanvas

object ViewUpdater {
	def updateView(viewData: ViewData): Unit = {
		AdjMatrixCanvas.setShapes(viewData.adjMatrix.shapes)
	}
}