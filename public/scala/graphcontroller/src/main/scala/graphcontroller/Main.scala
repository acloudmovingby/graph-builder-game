package graphcontroller

import scala.scalajs.js.annotation.*
import graphcontroller.render.{AdjMatrixCanvas, MainCanvas}
import graphcontroller.controller.AdjMatrixEventListeners
import graphcontroller.controller.{Controller, Initialization}

// Until we migrate fully to ScalaJS code, need to make this usable from the Vanilla JS side so it can access
// the graphcontroller instance
@JSExportTopLevel("Main")
object Main {
	private val graphController = new GraphController()

	def initializationParameters: Initialization = Initialization(
		adjMatrixWidth = AdjMatrixCanvas.canvas.offsetWidth.toInt,
		adjMatrixHeight = AdjMatrixCanvas.canvas.offsetHeight.toInt
	)

	// @main here indicates to run this method on startup of the ScalaJS application
	@main def start(): Unit = {
		MainCanvas.start()
		AdjMatrixCanvas.start()
		new AdjMatrixEventListeners().addEventListeners()

		println("GraphController ScalaJS application started.")
		Controller.handleEvent(initializationParameters)
	}

	@JSExport
	def getGraphController(): GraphController = graphController
}
