package graphcontroller.components.exportpane.eventlisteners

import graphcontroller.components.exportpane.ExportFormat.*
import org.scalajs.dom
import graphcontroller.controller.{ExportAdjacencyTypeChanged, CopyButtonClicked, Event, ExportFormatChanged}
import graphcontroller.controller.eventlisteners.EventListener
import graphcontroller.shared.AdjacencyExportType
import org.scalajs.dom.html.Select

object ExportPaneEventListeners extends EventListener {
	override def init(dispatch: Event => Unit): Unit = {
		copyButtonListener(dispatch)
		formatSelectionEventListener(dispatch)
		adjacencyTypeRadioListener(dispatch)
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
					case "java" => Java
					case other =>
						println(s"Unexpected format choice, defaulting to DOT: $other")
						DOT
				}
				dispatch(ExportFormatChanged(format))
			})
		}
	}

	private def adjacencyTypeRadioListener(dispatch: Event => Unit): Unit = {
		val listOption = dom.document.getElementById("list-option")
		val matrixOption = dom.document.getElementById("matrix-option")
		if (listOption != null) {
			listOption.addEventListener("change", (_: dom.Event) => {
				if (listOption.asInstanceOf[dom.html.Input].checked) {
					dispatch(ExportAdjacencyTypeChanged(AdjacencyExportType.List))
				}
			})
		}
		if (matrixOption != null) {
			matrixOption.addEventListener("change", (_: dom.Event) => {
				if (matrixOption.asInstanceOf[dom.html.Input].checked) {
					dispatch(ExportAdjacencyTypeChanged(AdjacencyExportType.Matrix))
				}
			})
		}
	}
}