package graphcontroller

import scala.scalajs.js.annotation.*
import graphcontroller.render.{AdjMatrixCanvas, MainCanvas}
import graphcontroller.controller.eventlisteners.{AdjMatrixEventListeners, MainCanvasEventListeners}
import graphcontroller.controller.{Controller, Initialization}

// Until we migrate fully to ScalaJS code, need to make this usable from the Vanilla JS side so it can access
// the graphcontroller instance
@JSExportTopLevel("Main")
object Main {
	private val graphController = new GraphController()

	/** Pass in parameters that are available at web page load (so we can program our code in a functional way,
	 * and we're not fetching info from the dom in the middle of our pure functions) */
	private def initializationEvent: Initialization = Initialization(
		adjMatrixWidth = AdjMatrixCanvas.canvas.offsetWidth.toInt,
		adjMatrixHeight = AdjMatrixCanvas.canvas.offsetHeight.toInt,
		padding = 20,
		numberPadding = 15
	)

	// @main here indicates to run this method on startup of the ScalaJS application
	@main def start(): Unit = {
		MainCanvas.start()
		AdjMatrixCanvas.start()
		new AdjMatrixEventListeners().addEventListeners()
		new MainCanvasEventListeners().addEventListeners()

		println("GraphController ScalaJS application started.")
		Controller.handleEvent(initializationEvent)
	}

	@JSExport
	def getGraphController(): GraphController = graphController
}
