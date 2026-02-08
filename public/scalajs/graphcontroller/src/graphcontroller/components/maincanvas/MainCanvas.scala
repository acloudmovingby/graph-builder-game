package graphcontroller.components.maincanvas

import graphcontroller.dataobject.canvas.{RenderOp}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas

object MainCanvas {
	/** Things to render on each animation frame callback */
	private var _shapes: Seq[RenderOp] = Seq.empty
	private var newShapes: Seq[RenderOp] = Seq.empty // will eventually replace _shapes once I convert all tools.js to ScalaJS
	private def getShapes = _shapes ++ newShapes
	val canvas: Canvas = dom.document.getElementById("main-canvas-lower").asInstanceOf[html.Canvas]
	val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
	val scale = dom.window.devicePixelRatio.toInt

	private def setCanvasSize(): Unit = {
		val canvasWidth = dom.window.innerWidth - 300 // infoPaneWidth is 300px
		val canvasHeight = dom.window.innerHeight
		canvas.style.width = s"${canvasWidth}px"
		canvas.style.height = s"${canvasHeight}px"

		// Set actual canvas size to scaled size for high-DPI displays (keeps edges looking sharp)
		canvas.width = (canvasWidth * scale).toInt
		canvas.height = (canvasHeight * scale).toInt
		ctx.scale(scale.toDouble, scale.toDouble)
	}

	/** Kickstart the requestAnimationFrame loop */
	def start(): Unit = {
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
		render(getShapes)

		// Schedule the next frame
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	// TODO once we totally convert to new way, we'll only need one of these
	def setShapesNew(shapes: Seq[RenderOp]): Unit = { newShapes = shapes }
	def setShapes(shapes: Seq[RenderOp]): Unit = { _shapes = shapes }
}
