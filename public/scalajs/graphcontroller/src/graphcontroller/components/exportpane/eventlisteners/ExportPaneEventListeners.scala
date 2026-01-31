package graphcontroller.components.exportpane.eventlisteners

import graphcontroller.components.exportpane.ExportFormat.*
import org.scalajs.dom
import graphcontroller.controller.{CopyButtonClicked, Event, ExportFormatChanged}
import graphcontroller.controller.eventlisteners.EventListener
import org.scalajs.dom.html.Select

object ExportPaneEventListeners extends EventListener {
	override def init(dispatch: Event => Unit): Unit = {
		copyButtonListener(dispatch)
		formatSelectionEventListener(dispatch)
	}

	private def copyButtonListener(dispatch: Event => Unit): Unit = {
		val btn = dom.document.getElementById("copy-btn")
		if (btn != null) {
			btn.addEventListener("click", (_: dom.Event) => dispatch(CopyButtonClicked))
		}
	}

	private def formatSelectionEventListener(dispatch: Event => Unit): Unit = {
		val select = dom.document.getElementById("export-format-select").asInstanceOf[Select]
		if (select != null) {
			select.addEventListener("change", (_: dom.Event) => {
				val format = select.value match {
					case "dot" => DOT
					case "json" => JSON
					case "python" => Python
					case "scala" => Scala
					case other =>
						println(s"Unexpected format choice, defaulting to DOT: $other")
						DOT
				}
				dispatch(ExportFormatChanged(format))
			})
		}
	}
}