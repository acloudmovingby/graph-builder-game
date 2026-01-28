package graphcontroller.components.exportpane

import scala.scalajs.js.Thenable.Implicits._
import org.scalajs.dom

import graphcontroller.components.Component
import graphcontroller.controller.{Event, CopyButtonClicked}
import graphcontroller.model.State

object ExportPane extends Component {

	// Needed for writing to clipboard which is done with a Future
	implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

	/** Pure function that takes current state and input event and produces new state */
	override def update(state: State, event: Event): State = {
		event match {
			case CopyButtonClicked =>
				println("Copy button clicked")
				state.copy(exportString = Some(state.graph.toDot))
			case _ =>
				// by setting this to None, we prevent copying to the clipboard everytime an event happens
				state.copy(exportString = None)
		}
	}

	/**
	 * Side-effectful function that renders to dom, writes to clipboard, etc. Keep as minimal as possible
	 * or have sub-methods that are pure functions
	 * */
	override def view(state: State): Unit = state.exportString match {
		case Some(text) =>
			println("Writing to clipboard...")
			dom.window.navigator.clipboard.writeText(text).recover {
				// TODO, later once I start using Try or an effect type at top-level we can hrow exception here or return effect
				_ => println("Clipboard write failed.")
			}
		case None => ()
	}
}

