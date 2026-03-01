package graphcontroller.components.maincanvas

import graphcontroller.dataobject.Vector2D
import graphcontroller.dataobject.Circle
import graphcontroller.dataobject.canvas.{CanvasRenderOp, CircleCanvas, TextCanvas}

enum NodeRenderStyle {
	case Basic, // The default look of a node, like your cursor is off the canvsa or using the basic edge adding tool
	BasicHover, // If you hover over a node when using the basic edge adding tool
	AddEdgeStart, // When you clicked on a node and enter basic edge adding mode
	AddEdgeNotStart, // All the other nodes that are waiting to be connected to
	AddEdgeHover, // When you are in edge adding mode and hovering over another target node
	AddEdgeHoverStart // For some reason in the initial implementation, we show hover effect on start node when using magic path tool
	                  // I'm not sure if it's necessary, but it does look okay so keeping it.
}

object NodeRender {
	// TODO uh, we should figure out why NodeRenderProperties has a different value than this
	val baseNodeRadius = 18
	val addEdgeInnerCircleRadius = 10
	val color1 = "#32BFE3" // "#32BFE3" is the blue color we use for the default nodes and hover effects in basic edge adding mode
	val color2 = "#FA5750" // "#FA5750" is the red color we use for the edge adding mode

	import NodeRenderStyle.*

	def createNodeCanvasObject(center: Vector2D, label: Option[String], style: NodeRenderStyle): Seq[CanvasRenderOp] = {
		def basicNodeCircle(center: Vector2D, color: String = color1) = {
			// creates a simple filled in circle of the specified color
			CircleCanvas(
				circ = Circle(center = center, radius = baseNodeRadius),
				fillColor = Some(color),
				borderColor = None, // Some(color1), // I don't know why but the vanilla JS code had stuff about borders but for some reason doesn't use them
				borderWidth = None // Some(8.0)
			)
		}

		def ringCircle(center: Vector2D) = CircleCanvas(
			circ = Circle(center, baseNodeRadius - 2),
			fillColor = Some("white"),
			borderColor = Some(color2),
			borderWidth = Some(4.0)
		)

		def basicHover(center: Vector2D, color: String = color1) = {
			val ringAroundNode = {
				CircleCanvas(
					circ = Circle(center = center, radius = baseNodeRadius + 6),
					fillColor = None,
					borderColor = Some(color),
					borderWidth = Some(4.0) // Some(8.0)
				)
			}
			Seq(basicNodeCircle(center, color), ringAroundNode)
		}

		lazy val basicText: Option[TextCanvas] = label.map { l =>
			TextCanvas(
				coords = center,
				text = l,
				color = "white",
				font = "1rem Arial"
			)
		}

		val circles = style match {
			case Basic => Seq(basicNodeCircle(center)) ++ basicText
			case BasicHover => basicHover(center) ++ basicText
			case AddEdgeStart =>
				val node = basicNodeCircle(center).copy(fillColor = Some(color2))
				Seq(node) ++ basicText
			case AddEdgeNotStart =>
				val node = ringCircle(center)
				val text = basicText.map(_.copy(color = color2))
				Seq(node) ++ text
			case AddEdgeHover =>
				val node = ringCircle(center)
				val innerCircle = CircleCanvas(
					circ = Circle(center, radius = addEdgeInnerCircleRadius),
					fillColor = Some(color2),
					borderColor = None,
					borderWidth = None
				)
				Seq(node, innerCircle) ++ basicText
			case AddEdgeHoverStart =>
					basicHover(center, color2) ++ basicText.map(_.copy(color = "white"))
		}
		circles
	}

}
