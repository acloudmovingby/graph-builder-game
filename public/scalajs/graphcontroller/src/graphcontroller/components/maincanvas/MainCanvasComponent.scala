package graphcontroller.components.maincanvas

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, MainCanvasMouseMove, MainCanvasMouseUp}
import graphcontroller.model.State
import graphcontroller.shared.{BasicTool, MagicPathTool}

object MainCanvasComponent extends Component {

	override def update(state: State, event: Event): State = {
		event match {
			case MainCanvasMouseMove(coords) =>
				val hoveringOnNode = state.keyToData.find { (key, data) =>
					val dx = coords.x - data.x
					val dy = coords.y - data.y
					val distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))
					distFromCent < NodeRender.baseNodeRadius * 2
				}.map(_._1)
				state.copy(hoveringOnNode = hoveringOnNode)
			case MainCanvasMouseUp(coords) =>
					val hoveringOnNode = state.keyToData.find { (key, data) =>
						val dx = coords.x - data.x
						val dy = coords.y - data.y
						val distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))
						distFromCent < NodeRender.baseNodeRadius * 2
					}.map(_._1)
//					hoveringOnNode.map { n =>
//						state.toolState match {
//							case BasicTool(None) =>
//								// start edge adding mode
//							case BasicTool(Some(edgeStart)) =>
//								// end edge adding
//								case _ => () // I dunno what happens here
//						}
//					}
					state.copy(hoveringOnNode = hoveringOnNode)
			case _ => state
		}
	}

	override def view(state: State): RenderOp = MainCanvasView.render(state)
}
