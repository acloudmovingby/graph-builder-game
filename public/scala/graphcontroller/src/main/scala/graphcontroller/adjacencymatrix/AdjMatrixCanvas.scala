package graphcontroller.adjacencymatrix

import graphcontroller.dataobject.canvas.{CanvasLine, RenderOp, TriangleCanvas}
import graphcontroller.dataobject.{Point, Triangle}
import org.scalajs.dom
import org.scalajs.dom.html

object AdjMatrixCanvas {
	/** Things to render on each animation frame callback */
	private var _shapes: Seq[RenderOp] = Seq.empty
	val canvas = dom.document.getElementById("adj-matrix").asInstanceOf[html.Canvas]
	val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
	val scale = dom.window.devicePixelRatio.toInt

	private def setCanvasSize(): Unit = {
		val canvasWidth = canvas.offsetWidth
		val canvasHeight = canvas.offsetHeight
		canvas.width = (canvasWidth * scale).toInt
		canvas.height = (canvasHeight * scale).toInt
		canvas.style.width = s"${canvasWidth}px"
		canvas.style.height = s"${canvasHeight}px"
		ctx.scale(scale.toDouble, scale.toDouble)
	}

	/** Kickstart the requestAnimationFrame loop */
	def start(): Unit = {
		println("Adj matrix starting...")
		setCanvasSize()
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	def render(shapes: Seq[RenderOp]): Unit = {
		// Clear the screen every frame
		ctx.clearRect(0, 0, canvas.width, canvas.height)

		shapes.foreach(_.draw(ctx))
	}

	// 4. The Loop
	// The timestamp is passed automatically by the browser (high-precision time)
	def loop(timestamp: Double): Unit = {

		// TODO delete this once I understand / use it
		// Update your game state here
		// For demo, let's just make a moving line based on time
		val offset = ((timestamp / 10) % 500).toInt
		val shapes = List(
			CanvasLine(from = Point(10 + offset, 10), to = Point(100 + offset, 100), 3, "red"),
			TriangleCanvas(Triangle(Point(200, 200), Point(250, 300), Point(150, 300)), "blue")
		)

		render(shapes)

		// Schedule the next frame
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	def setShapes(shapes: Seq[RenderOp]): Unit = { _shapes = shapes }
}
