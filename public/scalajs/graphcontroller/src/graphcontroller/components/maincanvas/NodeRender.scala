package graphcontroller.components.maincanvas

import graphcontroller.dataobject.Vector2D
import graphcontroller.dataobject.Circle
import graphcontroller.dataobject.canvas.{CanvasRenderOp, CircleCanvas, TextCanvas}

enum NodeRenderStyle {
	case Basic, // The default look of a node, like your cursor is off the canvsa or using the basic edge adding tool
	BasicHover, // If you hover over a node when using the basic edge adding tool
	AddEdgeStart, // When you clicked on a node and enter basic edge adding mode
	AddEdgeNotStart, // All the other nodes that are waiting to be connected to
	AddEdgeHover // When you are in edge adding mode and hovering over another target node
}

object NodeRender {

	// TODO uh, we should figure out why NodeRenderProperties has a different value than this
	val baseNodeRadius = 18
	val addEdgeInnerCircleRadius = 10

	import NodeRenderStyle.*

	def createNodeCanvasObject(center: Vector2D, label: Option[String], style: NodeRenderStyle): Seq[CanvasRenderOp] = {
		def basicNodeCircle(center: Vector2D) = {
			// creates a simple filled in circle of the specified color
			CircleCanvas(
				circ = Circle(center = center, radius = baseNodeRadius),
				fillColor = Some("#32BFE3"),
				borderColor = None, // Some("#32BFE3"), // I don't know why but the vanilla JS code had stuff about borders but for some reason doesn't use them
				borderWidth = None // Some(8.0)
			)
		}

		def ringCircle(center: Vector2D) = CircleCanvas(
			circ = Circle(center, baseNodeRadius - 2),
			fillColor = Some("white"),
			borderColor = Some("#FA5750"),
			borderWidth = Some(4.0)
		)

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
			case BasicHover =>
				val ringAroundNode = {
					CircleCanvas(
						circ = Circle(center = center, radius = baseNodeRadius + 6),
						fillColor = None,
						borderColor = Some("#32BFE3"),
						borderWidth = Some(4.0) // Some(8.0)
					)
				}
				Seq(basicNodeCircle(center), ringAroundNode) ++ basicText
			case AddEdgeStart =>
				val node = basicNodeCircle(center).copy(fillColor = Some("#FA5750"))
				Seq(node) ++ basicText
			case AddEdgeNotStart =>
				val node = ringCircle(center)
				val text = basicText.map(_.copy(color = "#FA5750"))
				Seq(node) ++ text
			case AddEdgeHover =>
				val node = ringCircle(center)
				val innerCircle = CircleCanvas(
					circ = Circle(center, radius = addEdgeInnerCircleRadius),
					fillColor = Some("#FA5750"),
					borderColor = None,
					borderWidth = None
				)
				Seq(node, innerCircle) ++ basicText
		}

		val textColor = "white" // TODO add the other possibilities as shown in JS code below

		/*
		if (labelsVisible) {
                ctx.font = "1rem Arial";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                const hasWhiteBackground = (inBasicEdgeMode || inMagicPathEdgeMode) && !isEdgeStart && nodes[i].key != nodeHover?.key;
                ctx.fillStyle = hasWhiteBackground ? "#FA5750" : "white";
                let label = nodes[i].key;
                const ADJUSTMENT = 1.5; // Ugh, textBaseline above doesn't help center on node properly so this makes it more centered
                ctx.fillText(label, nodes[i].data.x, nodes[i].data.y + ADJUSTMENT);
            }
		 */

		val text = label.map { labelText =>
			TextCanvas(
				coords = center,
				text = labelText,
				color = textColor,
				font = "1rem Arial"
			)
		}
		circles //++ text
	}

}
