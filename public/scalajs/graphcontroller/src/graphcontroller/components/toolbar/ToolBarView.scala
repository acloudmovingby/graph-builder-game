package graphcontroller.components.toolbar

import org.scalajs.dom
import org.scalajs.dom.html
import graphcontroller.components.RenderOp
import graphcontroller.shared.{AreaCompleteTool, BasicTool, MagicPathTool, MoveTool, SelectTool, Tool}

case class ToolBarRenderData(
	selectedTool: Tool,
	hoveringOnTool: Option[String],
	selectToolVisible: Boolean
) extends RenderOp {

	/** Right now, this is feature-flagged so hide if feature flag is disabled */
	def handleSelectTool(): Unit = {
		// TODO get this html id from the Tool class itself (maybe put in companion object)
		val selectTool = dom.document.getElementById("select").asInstanceOf[html.Button]
		if (selectTool != null) {
			if (selectToolVisible) {
				selectTool.style.display = "block"
			} else {
				selectTool.style.display = "none"
			}
		}
	}

	def render(): Unit = {
		handleSelectTool()

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
		val hotkeyElement = dom.document.getElementById("hover-info-hotkey").asInstanceOf[html.Paragraph]
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
				tool.hotkey match {
					case Some(char) =>
						hotkeyElement.style.visibility = "visible"
						hotkeyElement.innerHTML = char.toString
					case None => hotkeyElement.style.visibility = "hidden"
				}
			case None =>
				hoverInfoElement.style.visibility = "hidden"
				hotkeyElement.style.visibility = "hidden"
		}
	}
}

object AllTools {
	val tools: Map[String, Tool] = Map(
		"select" -> SelectTool(),
		"basic" -> BasicTool(None),
		"area-complete" -> AreaCompleteTool(false, Nil),
		"magic-path" -> MagicPathTool(None),
		"move" -> MoveTool(None)
	)
}
