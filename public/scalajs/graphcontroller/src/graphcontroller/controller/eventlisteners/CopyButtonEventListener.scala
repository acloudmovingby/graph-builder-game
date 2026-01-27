package graphcontroller.controller.eventlisteners

import org.scalajs.dom

import graphcontroller.controller.Controller
import graphcontroller.controller.ExportCopy

object CopyButtonEventListener {
	def init(): Unit = {
		val btn = dom.document.getElementById("copy-btn")
		if (btn != null) {
			btn.addEventListener("click", (_: dom.Event) => Controller.handleEvent(ExportCopy))
		}
	}
}

