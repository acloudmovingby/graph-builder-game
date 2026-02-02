package graphcontroller.components.exportpane

import scala.scalajs.js.Thenable.Implicits.*
import org.scalajs.dom
import org.scalajs.dom.html

import graphcontroller.components.Component
import graphcontroller.controller.{CopyButtonClicked, Event, ExportFormatChanged, ExportAdjacencyTypeChanged}
import graphcontroller.model.State
import graphcontroller.shared.AdjacencyExportType
import graphi.MapGraph

enum ExportFormat {
	case DOT, JSON, Python, Scala, Java
}

object ExportPane extends Component {

	// TODO: make this a def and adjust this URL to use the graph from this web app!! That would be cool
	val graphVizURL =
		"""
		  |https://dreampuf.github.io/GraphvizOnline/?engine=dot#graph%20%7B%0A%20n0%20--
		  |%20n2%20%0A%20n1%20--%20n2%20%0A%20n1%20--%20n3%20%0A%20n1%20--%20n26%20%0A%20n2%20--
		  |%20n3%20%0A%20n4%20--%20n14%20%0A%20n5%20--%20n6%20%0A%20n5%20--%20n7%20%0A%20n5%20--
		  |%20n8%20%0A%20n5%20--%20n9%20%0A%20n5%20--%20n10%20%0A%20n6%20--%20n7%20%0A%20n6%20--
		  |%20n8%20%0A%20n6%20--%20n9%20%0A%20n6%20--%20n10%20%0A%20n7%20--%20n8%20%0A%20n7%20--
		  |%20n9%20%0A%20n7%20--%20n10%20%0A%20n8%20--%20n9%20%0A%20n8%20--%20n10%20%0A%20n9%20--
		  |%20n10%20%0A%20n11%20--%20n15%20%0A%20n11%20--%20n17%20%0A%20n12%20--%20n13%20%0A%20n13%20--
		  |%20n14%20%0A%20n15%20--%20n16%20%0A%20n17%20--%20n18%20%0A%20n18%20--%20n19%20%0A%20n19%20--
		  |%20n20%20%0A%20n20%20--%20n21%20%0A%20n21%20--%20n22%20%0A%20n22%20--%20n23%20%0A%20n23%20--
		  |%20n24%20%0A%20n24%20--%20n25%20%0A%20n25%20--%20n26%20%0A%7D
		  |""".stripMargin

	import ExportFormat.*

	// Needed for writing to clipboard which is done with a Future
	implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

	private val MAX_LINES_PREVIEW = 20

	/** Pure function that takes current state and input event and produces new state */
	override def update(state: State, event: Event): State = {
		event match {
			// if copy button clicked, copy to clipboard
			case CopyButtonClicked => state.copy(copyToClipboard = true)
			// handle export format change
			case ExportFormatChanged(format) =>
				state.copy(exportFormat = format, copyToClipboard = false)
			// handle adjacency type change
			case ExportAdjacencyTypeChanged(adjType) =>
				state.copy(adjacencyExportType = adjType, copyToClipboard = false)
			// for all other events, unset that flag (so we don't keep copying to clipboard on every event
			case _ => state.copy(copyToClipboard = false)
		}
	}

	override def view(state: State): Unit = {
		def updateFormatDescription(format: ExportFormat): Unit = {
			val description = dom.document.getElementById("format-description").asInstanceOf[html.Paragraph]
			if (description != null) {
				val text = format match {
					case DOT =>
						// TODO the URL here is horrifyingly long, maybe we should put elsewhere or just go to a simpler graph on GraphViz
						s"""
						   |A standardized format for representing graphs, used by GraphViz and other applications.
						   |Try pasting <a href="$graphVizURL"
						   |target="_blank">here</a> and it will draw your graph.""".stripMargin
					case Java => "A HashMap for an adjacency list or a 2D array for an adjacency matrix."
					case _ => ""
				}
				description.innerHTML = text
			}
		}

		def updateSelectionOptions(format: ExportFormat): Unit = {
			val div = Option(dom.document.getElementById("matrix-or-list-selection-div").asInstanceOf[html.Div])

			// For export options besides DOT, be able to choose between adjacency list or adjacency matrix
			format match {
				case DOT => div.foreach {
					_.style.display = "none"
				}
				case _ => div.foreach {
					_.style.display = "block"
				}
			}
		}

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
					(lines.take(MAX_LINES_PREVIEW) :+ "  ...").mkString("\n")
				} else text
				previewElement.innerHTML = ExportStringGenerator.escapeHtml(trimmed)
			}
		}

		val exportString = ExportStringGenerator.generate(state.graph, state.exportFormat, state.adjacencyExportType)
		if (state.copyToClipboard) writeToClipboard(exportString)

		// TODO profile this and if it's an issue, then we can cache/memoize it,
		// if re-rendering the dom is the expensive part, we can perhaps simply cache it with some local state here, and compare the Strings
		// to decide if we want to re-render to the dom.
		// if calculating the export string is the expensive part, then we'll use a flag in state to indicate when graph has changed.
		renderPreview(exportString)
		updateFormatDescription(state.exportFormat)
		updateSelectionOptions(state.exportFormat)
	}
}
