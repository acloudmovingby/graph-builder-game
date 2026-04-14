package graphcontroller.components.maincanvas

import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{CanvasDoubleClick, DeleteSelectedNodes, Event, MainCanvasMouseEvent}
import graphcontroller.controller.MouseEvent.{Down, Leave, Move, Up}
import graphcontroller.dataobject.{Cell, NodeData, Vector2D}
import graphcontroller.model.{HoveredNode, State}
import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, SelectMode, SelectTool}

object MainCanvasComponent extends Component {

	/**
	 * Determine if point is inside the polygon.
	 *
	 * ray-casting algorithm based on
	 * https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html/pnpoly.html
	 */
	private def isInside(point: Vector2D, polygon: Seq[Vector2D]): Boolean = {
		val x = point.x
		val y = point.y
		var inside = false
		var i = 0
		var j = polygon.length - 1
		while (i < polygon.length) {
			val xi = polygon(i).x
			val yi = polygon(i).y
			val xj = polygon(j).x
			val yj = polygon(j).y
			val intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
			if (intersect) {
				inside = !inside
			}
			j = i
			i += 1
		}
		inside
	}

	private def mouseMoveHandling(state: State, event: MainCanvasMouseEvent): State = {
		val maybeHoveredNode = hoveredNode(event.coords, state.keyToData)

		val newState = state.toolState match {
			case tool: BasicTool => handleBasicTool(state, event, tool, maybeHoveredNode)
			case tool: MagicPathTool => handleMagicPathTool(state, event, tool, maybeHoveredNode)
			case tool: AreaCompleteTool => handleAreaCompleteTool(state, event, tool, maybeHoveredNode)
			case tool: MoveTool => handleMoveTool(state, event, tool, maybeHoveredNode)
			case tool: SelectTool => handleSelectTool(state, event , tool, maybeHoveredNode)
		}

		newState.copy(canvasInteraction = newState.canvasInteraction.copy(lastMousePosition = event.coords))
	}

	private def handleSelectTool(state: State, event: MainCanvasMouseEvent, tool: SelectTool, maybeHoveredNode: Option[Int]): State = {
		import SelectMode.*
		event.eventType match {
			case Down =>
				maybeHoveredNode match {
					case Some(node) =>
						if (event.shiftKey) {
							// Shift+click: toggle this node in/out of selection without clearing others
							val newSelection =
								if (state.selectedNodes.contains(node)) state.selectedNodes - node
								else state.selectedNodes + node
							state.copy(toolState = SelectTool(Idle), selectedNodes = newSelection)
						} else {
							// Normal click on a node: select just this one, clear others
							state.copy(toolState = SelectTool(Idle), selectedNodes = Set(node))
						}
					case None =>
						if (event.shiftKey) {
							// Shift+drag on empty canvas: keep existing selection, new box will ADD to it
							state.copy(toolState = SelectTool(DraggingBox(event.coords, state.selectedNodes)))
						} else {
							// Normal drag on empty canvas: clear selection and start a fresh box
							state.copy(toolState = SelectTool(DraggingBox(event.coords, Set.empty)), selectedNodes = Set.empty)
						}
				}
			case Up =>
				tool.mode match {
					case DraggingBox(startPoint, existingSelection) =>
						val newlySelected = state.nodesInRect(startPoint, event.coords)
						state.copy(toolState = SelectTool(Idle), selectedNodes = existingSelection ++ newlySelected)
					case _ =>
						state.copy(toolState = SelectTool(Idle))
				}
			case Move =>
				tool.mode match {
					case DraggingBox(startPoint, existingSelection) =>
						// Live preview: merge existing (pre-drag) selection with nodes currently inside the box
						val potentialSelection = state.nodesInRect(startPoint, event.coords)
						state.copy(selectedNodes = existingSelection ++ potentialSelection)
					case _ => state
				}
			case Leave => state.copy(toolState = SelectTool(Idle))
		}
	}

	private def handleBasicTool(state: State, event: MainCanvasMouseEvent, tool: BasicTool, maybeHoveredNode: Option[Int]): State = {
		event.eventType match {
			case Move =>
				(state.canvasInteraction.hoveredNode, maybeHoveredNode) match {
					case (Some(HoveredNode(prev, _)), Some(current)) if current == prev =>
						// It looks nicer if we don't immediately create hover effect right after clicking, so don't change HoveredNode's justAdded flag here
						state
					case _ =>
						state.copy(canvasInteraction = state.canvasInteraction.copy(hoveredNode = maybeHoveredNode.map(n => HoveredNode(n, false))))
				}
			case Down =>
				(maybeHoveredNode, tool.edgeStart) match {
					case (None, None) =>
						// Add new node!
						// Make the new node the hovered node, and set the justAdded flag to true (to avoid hover effect)
						state
							.addNode(event.coords)
							.copy(toolState = BasicTool(None))
							.setHoveredNode(Some(HoveredNode(state.graph.nodeCount, true)))
					case (None, Some(_)) =>
						// When clicking on the blank canvas, exit edge-adding mode
						state.copy(toolState = BasicTool(None))
					case (Some(hoveredNode), None) =>
						// Enter edge-adding mode
						state.copy(toolState = BasicTool(Some(hoveredNode)))
					case (Some(hoveredNode), Some(edgeStart)) =>
						if (hoveredNode == edgeStart) {
							// Exit edge adding mode
							state.copy(toolState = BasicTool(None))
						} else {
							// Add edge and reset start node to node just clicked
							state.addEdge(edgeStart, hoveredNode).copy(toolState = BasicTool(Some(hoveredNode)))
						}
				}
			case Up => state
			case Leave => state.copy(toolState = BasicTool(None))
		}
	}

	private def handleMagicPathTool(state: State, event: MainCanvasMouseEvent, tool: MagicPathTool, maybeHoveredNode: Option[Int]): State = {
		event.eventType match {
			case Move =>
				(maybeHoveredNode, tool.edgeStart) match {
					case (Some(currentlyHoveredNode), Some(edgeStart)) if currentlyHoveredNode != edgeStart =>
						// When using magic path tool, in edge adding mode, and we've now hovered over a _new_ node, then... add the edge! Magic!
						state.addEdge(edgeStart, currentlyHoveredNode)
							.copy(toolState = MagicPathTool(Some(currentlyHoveredNode)))
					case _ =>
						state.setHoveredNode(maybeHoveredNode.map(n => HoveredNode(n, false)))
				}
			case Down =>
				(maybeHoveredNode, tool.edgeStart) match {
					case (Some(hoveredNode), None) =>
						// enter edge-adding mode
						state.copy(toolState = MagicPathTool(Some(hoveredNode)))
					case (None, Some(_)) =>
						// exit edge-adding mode
						state.copy(toolState = MagicPathTool(None))
					case _ => state
				}
			case Up => state
			case Leave =>
				// exit edge-adding mode
				state.copy(toolState = MagicPathTool(None))
		}
	}

	private def handleAreaCompleteTool(state: State, event: MainCanvasMouseEvent, tool: AreaCompleteTool, maybeHoveredNode: Option[Int]): State = {
		event.eventType match {
			case Move =>
				if (tool.mousePressed) {
					state.copy(toolState = tool.copy(drawPoints = event.coords :: tool.drawPoints))
				} else {
					state.setHoveredNode(maybeHoveredNode.map(n => HoveredNode(n, false)))
				}
			case Down =>
				state.copy(toolState = AreaCompleteTool(true, event.coords :: Nil))
			case Up =>
				tool.drawPoints match {
					case first :: second :: _ if tool.mousePressed =>
						// If drawPoints is at least 2 elements, then calculate which nodes are inside selection area
						// Not: we're doing this in a fairly inefficient way, just checking all nodes (not using a quadtree or something fancy)
						val selectedNodes = state.keyToData.filter { case (_, nodeData) =>
							isInside(Vector2D(nodeData.x, nodeData.y), tool.drawPoints)
						}.keys.toSeq

						val newState = if (selectedNodes.length > 1) {
							val edges = for {
								node1 <- selectedNodes
								node2 <- selectedNodes
								if node1 != node2
							} yield (node1, node2)

							state.bulkUpdateEdges(edges, isAdd = true)
						} else {
							state
						}
						newState.copy(toolState = AreaCompleteTool(false, Nil))
					case _ =>
						// List has < 2 elements or the button wasn't pressed (like maybe it got pressed offscreen and then mouse moved on screen)
						// Either way, do nothing and reset Area Complete tool to base state
						state.copy(toolState = AreaCompleteTool(false, Nil))
				}
			case Leave =>
				state.copy(toolState = AreaCompleteTool(false, Nil))
		}
	}

	private def handleMoveTool(state: State, event: MainCanvasMouseEvent, tool: MoveTool, maybeHoveredNode: Option[Int]): State = {
		event.eventType match {
			case Move =>
				tool.node match {
					case Some(nodeBeingMoved) =>
						// In this case, mouse is being held down and we're currently moving a node around
						val currentData = state.keyToData(nodeBeingMoved)
						val newData = currentData.copy(x = event.coords.x, y = event.coords.y)
						state.copy(keyToData = state.keyToData.updated(nodeBeingMoved, newData))
					case None =>
						// Uh, I'm not 100% sure what this is about...I asked AI to refactor and only noticed this way later
						state.setHoveredNode(maybeHoveredNode.map(n => HoveredNode(n, false)))
				}
			case Down =>
				maybeHoveredNode match {
					case Some(hoveredNode) =>
						// We clicked on a node and we're starting to move it!
						state.pushUndoState.copy(toolState = MoveTool(Some(hoveredNode)))
					case None =>
						// We're resetting the tool state here (AI did that) but not sure that's necessary. Doesn't hurt I suppose
						state.copy(toolState = MoveTool(None))
				}
			case Up =>
				// When we release the mouse, forget any node that's being dragged (set it to None)
				state.copy(toolState = MoveTool(None))
			case Leave => state
		}
	}

	override def update(state: State, event: Event): State = {
		event match {
			case m: MainCanvasMouseEvent => mouseMoveHandling(state, m)
			case DeleteSelectedNodes if state.selectedNodes.nonEmpty =>
				// TODO: Actually remove nodes from graph once graphi library supports node removal
				println(s"Deleting nodes: ${state.selectedNodes.toSeq.sorted.mkString(", ")}")
				state.copy(selectedNodes = Set.empty)
			case CanvasDoubleClick(coords) =>
				state.toolState match {
					case _: SelectTool =>
						val maybeNode = hoveredNode(coords, state.keyToData)
						maybeNode match {
							case None =>
								// Double-click on empty space: add a node, stay in SelectTool
								state.addNode(coords)
							case Some(_) =>
								// Double-click on existing node: no-op for now
								// TODO: future — could open label editing
								state
						}
					case _ => state // double-click does nothing in other tools
				}
			case _ => state
		}
	}

	override def view(state: State): RenderOp = MainCanvasView.render(state)

	private def hoveredNode(coords: Vector2D, keyToData: Map[Int, NodeData]): Option[Int] = {
		keyToData.find { (key, data) =>
			val dx = coords.x - data.x
			val dy = coords.y - data.y
			val distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))
			distFromCent < NodeRender.baseNodeRadius * 2
		}.map(_._1)
	}
}
