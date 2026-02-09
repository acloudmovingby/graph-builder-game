package graphcontroller.components.exportpane

import scala.scalajs.js.Thenable.Implicits.*
import org.scalajs.dom
import org.scalajs.dom.html
import graphcontroller.components.{Component, RenderOp}
import graphcontroller.controller.{CopyButtonClicked, Event, ExportAdjacencyTypeChanged, ExportFormatChanged}
import graphcontroller.model.State
import graphcontroller.shared.GraphRepresentation
import graphi.MapGraph

enum ExportFormat {
	case DOT, JSON, Python, Scala, Java
}

object ExportPane extends Component {

	// TODO: make this a def and adjust this URL to use the graph from this web app!! That would be cool
	// Also, maybe just make a simpler graph so the URL is less long
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

	override def view(state: State): RenderOp = {
		def updateFormatDescription(format: ExportFormat): String = {
			format match {
				case DOT =>
					s"""
					   |A standardized format for representing graphs, used by GraphViz and other applications.
					   |Try pasting <a href="$graphVizURL" target="_blank">here</a>.""".stripMargin
				case Java => "A HashMap for an adjacency list or a 2D array for an adjacency matrix."
				case JSON => "Note: JSON requires keys to be quoted, so all node indices will be quoted when exporting an adjacency list."
				case _ => ""
			}
		}

		def updateSelectionOptions(format: ExportFormat): Boolean = {
			format match {
				case Python | Java | Scala | JSON => true
				case DOT => false
			}
		}

		def writeToClipboard(text: String): Unit = {
			dom.window.navigator.clipboard.writeText(text).recover {
				// TODO, later once I start using Try or an effect type at top-level we can hrow exception here or return effect
				_ => println("Clipboard write failed.")
			}
		}

		def renderPreview(text: String): String = {
			val lines = text.linesIterator.toSeq
			if (lines.length > MAX_LINES_PREVIEW) {
				(lines.take(MAX_LINES_PREVIEW) :+ s"${ExportStringGenerator.INDENT}...").mkString("\n")
			} else text
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

		ExportPaneRenderData(
			descriptionText = updateFormatDescription(state.exportFormat),
			shouldShowGraphRepresentationOptions = updateSelectionOptions(state.exportFormat),
			previewText = renderPreview(exportString),
			clipboardContent = if (state.copyToClipboard) Some(exportString) else None
		)
	}
}
