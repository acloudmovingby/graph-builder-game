package graphcontroller.components.buildpane

import graphcontroller.components.ops.{SetAttribute, SetInnerHTML, SetStyleProperty}
import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, HoverDirectednessIcon, ToggleDirectedness, ToggleLabelsVisibility}
import graphcontroller.model.State
import graphi.{DirectedMapGraph, SimpleMapGraph}

object BuildPaneComponent extends Component {
	private def toggleDirectionality(state: State): State = {
		val newGraph = state.graph match {
			case g: DirectedMapGraph[Int] =>
				var undirectedGraph = new SimpleMapGraph[Int]()
				// add all nodes
				for (node <- g.adjMap.keys) {
					undirectedGraph = undirectedGraph.addNode(node)
				}
				// add all edges in undirected manner
				for {
					(from, neighbors) <- g.adjMap
					to <- neighbors
				} {
					undirectedGraph = undirectedGraph.addEdge(from, to)
				}
				undirectedGraph
			case g: SimpleMapGraph[Int] =>
				// Note: the simple graph's adjacency lists are always bidirectional, which means that when we convert to directed
				// it will fill in all directions. So if you toggle back and forth, it will lose the directional information
				// from the directed graph. There's no good way to avoid this, you could keep a separate parallel graph that has
				// the old information but then what happens if you add edges in simple mode?
				//
				// Since this toggle directedness operation is undoable, and it's a nice way to "fill in" all the edges,
				// this is the behavior we'll do for now)
				new DirectedMapGraph[Int](g.adjMap)
		}
		state
			.pushUndoState // because toggling directedness cause information to be lost, make it undoable
			.copy(graph = newGraph)
	}

	override def update(state: State, event: Event): State = {
		event match {
			case ToggleLabelsVisibility =>
				state.copy(labelsVisible = !state.labelsVisible)
			case ToggleDirectedness => toggleDirectionality(state)
			case HoverDirectednessIcon(isHover) => state.copy(hoverDirectedIcon = isHover)
			case _ => state
		}
	}

	override def view(state: State): RenderOp = {
		val nodeLabelToggleIcon = if (state.labelsVisible) "images/node-label-visible.svg" else "images/invisible-icon.svg"

		val directedToggleIcon = if (state.isDirected) "images/arrow-small-1-blue.svg" else "images/arrow-small-1.svg"
		val directedToggleBtnBgColor = (state.isDirected, state.hoverDirectedIcon) match {
			case (true, false) => "rgb(235, 250, 255)" // "#cff5ff"
			case (true, true) => "#cce8f0"
			case (false, false) => "white"
			case (false, true) => "lightgray"
		}

		BuildPaneRenderOp(
			Seq(
				SetAttribute("visible-icon", "src", nodeLabelToggleIcon),
				SetAttribute("directed-icon", "src", directedToggleIcon),
				SetInnerHTML("node-count", state.graph.nodeCount.toString),
				SetInnerHTML("edge-count", state.graph.edgeCount.toString),
				SetStyleProperty("directed-btn", "background-color", directedToggleBtnBgColor)
			)
		)
	}
}

case class BuildPaneRenderOp(ops: Seq[RenderOp]) extends RenderOp {
	override def render(): Unit = ops.foreach(_.render())
}