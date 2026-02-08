package graphcontroller.components.exportpane

import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.Thenable.Implicits.thenable2future

import graphcontroller.components.RenderOp

case class ExportPaneRenderData(
	descriptionText: String, // the commentary text that accompanies the export format
	shouldShowGraphRepresentationOptions: Boolean, // whether to show the options for exporting as an adjacency list vs matrix (only relevant for some export formats)
	previewText: String, // The preview of the code that gets exported (copied to clipboard)
	clipboardContent: Option[String] // the actual content that should be copied to clipboard, which may be different from the preview text if the preview is truncated for long graphs
) extends RenderOp {
	implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

	def render(): Unit = {
		Option(dom.document.getElementById("matrix-or-list-selection-div").asInstanceOf[html.Div])
			.foreach { div =>
				div.style.display = if (shouldShowGraphRepresentationOptions) "block" else "none"
			}

		Option(dom.document.getElementById("format-description").asInstanceOf[html.Paragraph])
			.foreach { description =>
				description.innerHTML = descriptionText
			}

		Option(dom.document.getElementById("graph-export-string").asInstanceOf[html.Paragraph])
			.foreach { previewElement =>
				previewElement.innerHTML = ExportStringGenerator.escapeHtml(previewText)
			}

		clipboardContent.foreach { content =>
			dom.window.navigator.clipboard.writeText(content).recover {
				_ => println("Clipboard write failed.")
			}
		}
	}
}
