package graphcontroller

import scala.scalajs.js.annotation.*
import graphcontroller.adjacencymatrix.AdjMatrixCanvas
import graphcontroller.render.MainCanvas

// Until we migrate fully to ScalaJS code, need to make this usable from the Vanilla JS side so it can access
// the graphcontroller instance
@JSExportTopLevel("Main")
object Main {
	private val graphController = new GraphController()

	// @main here indicates to run this method on startup of the ScalaJS application
	// needs scalaJSUseMainModuleInitializer = true
	@main def start(): Unit = {
		MainCanvas.start()
		AdjMatrixCanvas.start()
		new AdjMatrixEventListeners(graphController).setupEventListeners()
	}

	@JSExport
	def getGraphController(): GraphController = graphController
}
