package graphcontroller.controller

import org.scalajs.dom
import org.scalajs.dom.html

class AdjMatrixEventListeners {
	private val adjMatrixCanvas = dom.document.getElementById("adj-matrix").asInstanceOf[html.Canvas]
	private val scale = dom.window.devicePixelRatio

	private def calculateEventCoordinates(e: dom.MouseEvent): (Int, Int) = {
		val rect = adjMatrixCanvas.getBoundingClientRect()
		val x = (e.clientX - rect.left).toInt
		val y = (e.clientY - rect.top).toInt
		(x, y)
	}

	private def canvasWidth: Int = (adjMatrixCanvas.width / scale).toInt

	private def canvasHeight: Int = (adjMatrixCanvas.height / scale).toInt

	private def inBounds(x: Int, y: Int): Boolean = {
		x >= 0 && x <= canvasWidth && y >= 0 && y <= canvasHeight
	}

	def addEventListeners(): Unit = {
		// Resize listener
		adjMatrixCanvas.addEventListener("mousemove", (e: dom.Event) => {
			val (x, y) = calculateEventCoordinates(e.asInstanceOf[dom.MouseEvent])
			if (inBounds(x, y)) {
				//println(s"AdjMatrixEventListeners - mousemove event detected: x=$x, y=$y, canvasWidth=${adjMatrixCanvas.width}, canvasHeight=${adjMatrixCanvas.height}")
				val event = AdjMatrixMouseMove(x, y)
				Controller.handleEvent(event)
			}
		})

		adjMatrixCanvas.addEventListener("mouseleave", (e: dom.Event) => {
			Controller.handleEvent(AdjMatrixMouseLeave)
		})

		adjMatrixCanvas.addEventListener("mouseup", (_: dom.Event) => {
			Controller.handleEvent(AdjMatrixMouseUp)
		})

		adjMatrixCanvas.addEventListener("mousedown", (_: dom.Event) => {
			Controller.handleEvent(AdjMatrixMouseDown)
		})
	}
}
