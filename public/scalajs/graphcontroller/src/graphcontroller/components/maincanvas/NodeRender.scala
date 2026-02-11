package graphcontroller.components.maincanvas

import graphcontroller.dataobject.Vector2D
import graphcontroller.dataobject.Circle
import graphcontroller.dataobject.canvas.{CanvasRenderOp, CircleCanvas, TextCanvas}

enum NodeRenderStyle {
	case Basic, BasicHover, AddEdgeStart, AddEdgeOther, AddEdgeHover
}

object NodeRender {
	import NodeRenderStyle.*

	def createNodeCanvasObject(center: Vector2D, label: Option[String], style: NodeRenderStyle): Seq[CanvasRenderOp] = {
		val circles = style match {
			case Basic =>
				Seq(CircleCanvas(
					circ = Circle(center = center, radius = 18),
					fillColor = Some("#32BFE3"),
					borderColor = None, // Some("#32BFE3"), // I don't know why but the vanilla JS code had stuff about borders but for some reason doesn't use them
					borderWidth = None // Some(8.0)
				))
			case _ => Seq.empty // TODO
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
				font =  "1rem Arial"
			)
		}
		circles ++ text
	}

}
