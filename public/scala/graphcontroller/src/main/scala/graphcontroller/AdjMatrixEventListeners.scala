package graphcontroller

import graphcontroller.GraphController
import org.scalajs.dom
import org.scalajs.dom.html

class AdjMatrixEventListeners(graphController: GraphController) {
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

	def setupEventListeners(): Unit = {
		// Resize listener
		adjMatrixCanvas.addEventListener("mousemove", (e: dom.Event) => {
			val (x, y) = calculateEventCoordinates(e.asInstanceOf[dom.MouseEvent])
				if (inBounds(x, y)) println(s"AdjMatrixEventListeners - mousemove event detected: x=$x, y=$y, canvasWidth=${adjMatrixCanvas.width}, canvasHeight=${adjMatrixCanvas.height}")
		})

		adjMatrixCanvas.addEventListener("mouseleave", (e: dom.Event) => {
			println(s"AdjMatrixEventListeners - mouseleave event detected: ${e.asInstanceOf[dom.MouseEvent]}")
		})

		adjMatrixCanvas.addEventListener("mouseup", (_: dom.Event) => {
			println("AdjMatrixEventListeners - mouseup event detected")
		})
	}
}
