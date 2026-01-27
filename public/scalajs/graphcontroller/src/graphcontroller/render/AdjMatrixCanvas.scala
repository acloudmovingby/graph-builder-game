package graphcontroller.render

import graphcontroller.dataobject.canvas.{CanvasLine, RenderOp, TriangleCanvas}
import graphcontroller.dataobject.{Triangle, Vector2D}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas

// TODO:
//  (1) align this better with the general Controller => Model => View architecture and have requestAnimationFrame calls be Tick events
//      have width/height come from initialization or resize events rather than reading from the DOM element directly
//  (2) Make parent trait that both MainCanvas and this extend
object AdjMatrixCanvas {
	/** Things to render on each animation frame callback */
	private var shapes: Seq[RenderOp] = Seq.empty
	val canvas: Canvas = dom.document.getElementById("adj-matrix").asInstanceOf[html.Canvas]
	private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
	private val scale = dom.window.devicePixelRatio.toInt

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
		// TODO move this to the controller as a Tick event
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	// Not using timestamp for now, but will be useful later for animations
	def loop(timestamp: Double): Unit = {
		// Clear the screen every frame (for now)
		ctx.clearRect(0, 0, canvas.width, canvas.height)
		shapes.foreach(_.draw(ctx))

		// Schedule the next frame
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	def setShapes(shapes: Seq[RenderOp]): Unit = { this.shapes = shapes }
}
