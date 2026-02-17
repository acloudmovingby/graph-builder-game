package graphcontroller.components.maincanvas

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, MainCanvasMouseEvent}
import graphcontroller.controller.MouseEventType.{Down, Leave, Move, Up}
import graphcontroller.dataobject.{NodeData, Vector2D}
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{BasicTool, MagicPathTool}

object MainCanvasComponent extends Component {
	private def mouseMoveHandling(state: State, event: MainCanvasMouseEvent): State = {
		val (coords, eventType) = (event.coords, event.eventType)
		val maybeHoveredNode = hoveringOnNode(coords, state.keyToData)
		val newState = eventType match {
			case Move =>
				(state.hoveringOnNode, maybeHoveredNode) match {
					case (Some(HoveredNode(prev, _)), Some(current)) if current == prev => state
					case _ => state.copy(hoveringOnNode = maybeHoveredNode.map(n => HoveredNode(n, false)))
				}
			case Up => state
			case Down =>
				(maybeHoveredNode, state.toolState) match {
					case (None, BasicTool(None)) =>
						// add new node, flip stillInNode flag, make current node the hovered node
						state
							.addNode(coords)
							.copy(
								toolState = BasicTool(None),
								hoveringOnNode = Some(HoveredNode(state.graph.nodeCount, true))
							)
					case (None, b@BasicTool(Some(_))) =>
						// exit edge-adding node (change tool state), otherwise changed
						state.copy(toolState = b.copy(edgeStart = None))
					case (Some(hoveredNode), BasicTool(None)) =>
						// enter edge-adding mode (change tool state) otherwise state unchanged
						state.copy(toolState = BasicTool(Some(hoveredNode)))
					case (Some(hoveredNode), BasicTool(Some(edgeStart))) =>
						if (hoveredNode == edgeStart) {
							// exit edge adding mode
							state.copy(toolState = BasicTool(None))
						} else {
							// add edge and reset start node to node just clicked (keep in BasicTool tool state)
							state.addEdge(edgeStart, hoveredNode).copy(toolState = BasicTool(Some(hoveredNode)))
						}
					case _ =>
						// TODO handle other tool states and scenarios
						state
				}
			case Leave => state.toolState match {
				case BasicTool(Some(_)) => state.copy(toolState = BasicTool(None))
				case MagicPathTool(Some(_)) => state.copy(toolState = MagicPathTool(None))
				case _ => state
			}
		}
		// TODO delete this logging
		//		if (state.graph != newState.graph || state.toolState != newState.toolState || state.hoveringOnNode != newState.hoveringOnNode) {
		//			println(s"--------- $eventType ---------")
		//			println(s"PREVIOUS graph state: ${state.graph}, previous tool state: ${state.toolState}, previous hoveringOnNode: ${state.hoveringOnNode}")
		//			println(s"graph state: ${newState.graph}, tool state: ${newState.toolState}, hoveringOnNode=${newState.hoveringOnNode}")
		//		}

		// handle updating the stored mouse position and return
		newState.copy(lastMainCanvasMousePosition = event.coords)
	}

	override def update(state: State, event: Event): State = {
		event match {
			case m: MainCanvasMouseEvent => mouseMoveHandling(state, m)
			case _ => state
		}
	}

	override def view(state: State): RenderOp = MainCanvasView.render(state)

	private def hoveringOnNode(coords: Vector2D, keyToData: Map[Int, NodeData]): Option[Int] = {
		val hoveringOnNode = keyToData.find { (key, data) =>
			val dx = coords.x - data.x
			val dy = coords.y - data.y
			val distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))
			distFromCent < NodeRender.baseNodeRadius * 2
		}.map(_._1)
		hoveringOnNode
	}
}
