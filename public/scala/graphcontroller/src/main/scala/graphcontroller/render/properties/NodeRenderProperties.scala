package graphcontroller.render.properties

case class NodeRenderProperties(nodeRadius: Int)

object NodeRenderProperties {
	val default = NodeRenderProperties(nodeRadius = 15)
}
