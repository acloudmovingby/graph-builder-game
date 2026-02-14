package graphcontroller.shared

import graphcontroller.dataobject.Vector2D

sealed trait Tool {
	val htmlId: String
	val header: String
	val cursorIconPath: String
	val animationImgPath: String
	val description: String
}

case class BasicTool(
	edgeStart: Option[Int] // Only if in edge adding mode (where a start edge has been chosen)
) extends Tool {
	override val header: String = "Basic Node/Edge Adding Tool"
	override val animationImgPath: String = "images/basic-tool-tooltip-example.gif"
	override val htmlId: String = "basic"
	override val cursorIconPath: String = "url('images/pointer.svg'), pointer"
	override val description: String = "Click to make nodes, then click on a node to begin adding edges. To exit edge making mode, simply click on the gray canvas."
}

case class AreaCompleteTool(
	mousePressed: Boolean,
	drawPoints: Seq[Vector2D]
) extends Tool {
	override val header: String = "Area Complete Tool"
	override val animationImgPath: String = "images/area-complete-tool-tooltip-example.gif"
	override val htmlId: String = "area-complete"
	override val cursorIconPath: String = "url('images/area-complete-cursor.svg'), pointer"
	override val description: String = "Adds all possible edges between nodes in the selected area."
}

case class MagicPathTool(
	edgeStart: Option[Int],
) extends Tool {
	override val htmlId: String = "magic-path"
	override val header: String = "Magic Path Tool"
	override val cursorIconPath: String = "url('images/magic-path-cursor-2.svg'), pointer"
	override val animationImgPath: String = "images/magic-path-tool-tooltip-example.gif"
	override val description: String = "Click on a node then simply move the mouse to other nodes to automatically build a path! No need to drag or click. Magic!"
}

case class MoveTool(
	node: Option[Int]
) extends Tool {
	override val htmlId: String = "move"
	override val header: String = "Move Tool"
	override val cursorIconPath: String = "url('images/move-tool-cursor.svg'), pointer"
	override val animationImgPath: String = "images/move-tool-tooltip-example.gif"
	override val description: String = "Click and drag it around."
}