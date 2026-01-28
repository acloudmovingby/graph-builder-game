package graphcontroller.components.exportpane

import scala.scalajs.js.Thenable.Implicits.*
import org.scalajs.dom
import org.scalajs.dom.html

import graphcontroller.components.Component
import graphcontroller.controller.{CopyButtonClicked, Event, ExportFormatChanged}
import graphcontroller.model.State
import graphi.MapGraph

enum ExportFormat {
	case DOT, JSON, Python, Scala
}

object ExportPane extends Component {

	import ExportFormat.*

	// Needed for writing to clipboard which is done with a Future
	implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

	private val MAX_LINES_PREVIEW = 3

	/** Pure function that takes current state and input event and produces new state */
	override def update(state: State, event: Event): State = {
		event match {
			// if copy button clicked, copy to clipboard
			case CopyButtonClicked => state.copy(copyToClipboard = true)
			// for all other events, unset that flag (so we don't keep copying to clipboard on every event
			case _ => (event match {
				case ExportFormatChanged(format) =>
					state.copy(exportFormat = format)
				case _ => state
			}).copy(copyToClipboard = false) // here, unset flag
		}
	}

	private def generateExportString(graph: MapGraph[Int, ?], format: ExportFormat): String = {
		format match {
			case DOT => graph.toDot
			case _ => "Unimplemented"
		}
	}

	/**
	 * Side-effectful function that renders to dom, writes to clipboard, etc. Keep as minimal as possible
	 * or have sub-methods that are pure functions
	 * */
	override def view(state: State): Unit = {
		def writeToClipboard(text: String): Unit = {
			dom.window.navigator.clipboard.writeText(text).recover {
				// TODO, later once I start using Try or an effect type at top-level we can hrow exception here or return effect
				_ => println("Clipboard write failed.")
			}
		}

		def renderPreview(text: String): Unit = {
			val previewElement = dom.document.getElementById("graph-export-string").asInstanceOf[html.Paragraph]
			if (previewElement != null) {
				val lines = text.linesIterator.toSeq
				val trimmed = if (lines.length > MAX_LINES_PREVIEW) {
					(lines.take(MAX_LINES_PREVIEW) :+ "...").mkString("\n")
				} else text
				previewElement.innerHTML = trimmed
			}
		}

		val exportString = generateExportString(state.graph, state.exportFormat)
		if (state.copyToClipboard) writeToClipboard(exportString)

		// TODO profile this and if it's an issue, then we can cache/memoize it,
		// if re-rendering the dom is the expensive part, we can perhaps simply cache it with some local state here, and compare the Strings
		// to decide if we want to re-render to the dom.
		// if calculating the export string is the expensive part, then we'll use a flag in state to indicate when graph has changed.
		renderPreview(exportString)
	}
}


