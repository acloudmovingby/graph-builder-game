package graphcontroller.components.toolbar

import org.scalajs.dom
import org.scalajs.dom.html
import graphcontroller.components.RenderOp

import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, Tool}

case class ToolBarRenderData(
	selectedTool: Tool,
	hoveringOnTool: Option[String]
) extends RenderOp {
	def render(): Unit = {
		val toolBar = dom.document.querySelector(".toolbar")
		val buttons = toolBar.querySelectorAll("button")
		buttons.foreach {
			case btn: html.Button =>
				if (btn.id == selectedTool.htmlId) {
					btn.classList.add("selected")
				} else {
					btn.classList.remove("selected")
				}
		}

		val hoverInfoElement = dom.document.getElementById("hover-info-pane").asInstanceOf[html.Div]
		hoveringOnTool match {
			case Some(toolId) =>
				val tool = AllTools.tools(toolId)
				val toolBtn = dom.document.getElementById(tool.htmlId).asInstanceOf[html.Button]
				val toolBtnOffsetLeft = toolBtn.offsetLeft
				val toolBtnWidth = toolBtn.offsetWidth
				val toolBtnHeight = toolBtn.offsetHeight
				hoverInfoElement.style.left = s"${toolBtnOffsetLeft + toolBtnWidth / 2}px"
				hoverInfoElement.style.top = s"${toolBtnHeight - 5}px"
				hoverInfoElement.style.visibility = "visible"
				dom.document.getElementById("hover-header").innerHTML = tool.header
				dom.document.getElementById("hover-description").innerHTML = tool.description
				dom.document.getElementById("hover-info-img").asInstanceOf[html.Image].src = tool.animationImgPath
			case None =>
				hoverInfoElement.style.visibility = "hidden"
		}
	}
}

object AllTools {
	val tools: Map[String, Tool] = Map(
		"basic" -> BasicTool(None),
		"area-complete" -> AreaCompleteTool(false, Seq.empty),
		"magic-path" -> MagicPathTool(None),
		"move" -> MoveTool(None)
	)
}
