package graphcontroller.render

import scala.scalajs.js.Thenable.Implicits._
import org.scalajs.dom

object ClipboardWriter {
	implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

	private var lastCopiedText: String = ""

	def writeToClipboard(text: String): Unit = {
		if (text != lastCopiedText) {
			lastCopiedText = text
			dom.window.navigator.clipboard.writeText(text).recover {
				case _ => println("Clipboard write failed.")
			}
		}
	}
}
