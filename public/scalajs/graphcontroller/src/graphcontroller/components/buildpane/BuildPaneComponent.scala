package graphcontroller.components.buildpane

import graphcontroller.components.ops.{SetAttribute, SetInnerHTML, SetStyleProperty}
import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{Event, ToggleDirectedness, ToggleLabelsVisibility}
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
				// Note: this will make every edge a bidirectional (there's no good way to avoid this, you could
				// retain information if you toggled, but since this is undoable and it's a nice way to "fill in" all
				// edges, this is the behavior we'll have for now)
				new DirectedMapGraph[Int](g.adjMap)
		}
		state
			.pushUndoState // because toggling directedness loses information, make it undoable
			.copy(graph = newGraph)
	}

	override def update(state: State, event: Event): State = {
		event match {
			case ToggleLabelsVisibility =>
				state.copy(labelsVisible = !state.labelsVisible)
			case ToggleDirectedness => toggleDirectionality(state)
			case _ => state
		}
	}

	override def view(state: State): RenderOp = {
		/*
		 if (document.getElementById("directed-icon")) {
			document.getElementById("directed-icon").src = graphController.isDirected() ?
				"images/arrow-small-1-blue.svg" :
				"images/arrow-small-1.svg";
			}
			if (document.getElementById("directed-btn")) {
				document.getElementById("directed-btn").style.backgroundColor = graphController.isDirected() ?
					"#cff5ff" :
					"white";
			}
		 */
		val nodeLabelToggleIcon = if (state.labelsVisible) "images/node-label-visible.svg" else "images/invisible-icon.svg"

		val directedToggleIcon = if (state.isDirected) "images/arrow-small-1-blue.svg" else "images/arrow-small-1.svg"
		val directedToggleBtnBgColor = if (state.isDirected) "#cff5ff" else "white"

		BuildPaneRenderOp(
			Seq(
				SetAttribute("visible-icon", "src", nodeLabelToggleIcon),
				SetInnerHTML("node-count", state.graph.nodeCount.toString),
				SetInnerHTML("edge-count", state.graph.edgeCount.toString)
			)
		)
	}
}

case class BuildPaneRenderOp(ops: Seq[RenderOp]) extends RenderOp {
	override def render(): Unit = ops.foreach(_.render())
}

case class DirectedToggleButtonRenderOp(isDirected: Boolean) extends RenderOp {
	override def render(): Unit = {
		val directedToggleIcon = if (isDirected) "images/arrow-small-1-blue.svg" else "images/arrow-small-1.svg"
		val directedToggleBtnBgColor = if (isDirected) "#cff5ff" else "white"

		SetAttribute("directed-icon", "src", directedToggleIcon).render()
		SetStyleProperty("directed-btn", "background-color", directedToggleBtnBgColor).render()
	}
}