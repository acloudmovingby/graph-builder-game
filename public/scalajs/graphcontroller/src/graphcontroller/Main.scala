package graphcontroller

import graphcontroller.components.Component
import graphcontroller.components.exportpane.ExportPane
import graphcontroller.components.exportpane.eventlisteners.CopyButtonEventListener

import scala.scalajs.js.annotation.*
import graphcontroller.render.{AdjMatrixCanvas, MainCanvas}
import graphcontroller.controller.eventlisteners.{AdjMatrixEventListeners, EventListener, MainCanvasEventListeners}
import graphcontroller.controller.{Controller, Initialization}

// Until we migrate fully to ScalaJS code, need to make this usable from the Vanilla JS side so it can access
// the graphcontroller instance
@JSExportTopLevel("Main")
object Main {
	private val graphController = new GraphController()
	
	private val eventListeners: Seq[EventListener] = Seq(CopyButtonEventListener)

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
		/* The following stuff is the old way of doing it in the layers architecture where everything was 
		* spread out and not co-located like in the new 'components' architecture */
		MainCanvas.start()
		AdjMatrixCanvas.start()
		new AdjMatrixEventListeners().addEventListeners()
		new MainCanvasEventListeners().addEventListeners()
		
		/* 'New' components architecture. Wiring up the components */
		eventListeners.foreach { c => c.init(Controller.handleEvent) }

		println("GraphController ScalaJS application started.")
		Controller.handleEvent(initializationEvent)
	}

	@JSExport
	def getGraphController(): GraphController = graphController
}
