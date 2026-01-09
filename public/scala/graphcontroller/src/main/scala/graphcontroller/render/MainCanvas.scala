package graphcontroller.render

import org.scalajs.dom
import org.scalajs.dom.html
import graphcontroller.dataobject.canvas.{CanvasLine, RenderOp, TriangleCanvas}
import graphcontroller.dataobject.{Point, Triangle}

object MainCanvas {
	/** Things to render on each animation frame callback */
	private var _shapes: Seq[RenderOp] = Seq.empty
	val canvas = dom.document.getElementById("overlay-canvas").asInstanceOf[html.Canvas]
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

		shapes.foreach {
			case CanvasLine(from, to, width, color) =>
				ctx.beginPath()
				ctx.strokeStyle = color
				ctx.lineWidth = width
				ctx.moveTo(from.x, from.y)
				ctx.lineTo(to.x, to.y)
				ctx.stroke()

			case TriangleCanvas(Triangle(pt1, pt2, pt3), color) =>
				ctx.beginPath()
				ctx.fillStyle = color
				ctx.moveTo(pt1.x, pt1.y)
				ctx.lineTo(pt2.x, pt2.y)
				ctx.lineTo(pt3.x, pt3.y)
				ctx.fill()
		}
	}

	// 4. The Loop
	// The timestamp is passed automatically by the browser (high-precision time)
	def loop(timestamp: Double): Unit = {

		// TODO delete this once I understand / use it
		// Update your game state here
		// For demo, let's just make a moving line based on time
//		val offset = ((timestamp / 10) % 500).toInt
//		val shapes = List(
//			CanvasLine(from = Point(10 + offset, 10), to = Point(100 + offset, 100), 3, "red"),
//			TriangleCanvas(Triangle(Point(200, 200), Point(250, 300), Point(150, 300)), "blue")
//		)

		render(_shapes)

		// Schedule the next frame
		dom.window.requestAnimationFrame(timestamp => loop(timestamp))
	}

	def setShapes(shapes: Seq[RenderOp]): Unit = { _shapes = shapes }
}
