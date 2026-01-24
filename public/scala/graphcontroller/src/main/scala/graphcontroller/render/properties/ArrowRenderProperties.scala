package graphcontroller.render.properties

import graphcontroller.dataobject.{Vector2D, Triangle}

/*
displacement = node radius + padding + triangle height

			 |  .  |  <- node center
			  \___/   <- edge of node
				|
				| <- arrowPadding (empty space)
				|
				/\    |
			   /  \   | <- triangle height (i.e. arrow head)
  			  /____\  |
				|

*/

case class ArrowRenderProperties(
	height: Int,
	base: Int,
	scaleFactor: Int,
	displacement: Int,
	color: String
) {
	/** Base triangle for arrow. Changes to the size/proportion of this Triangle will be reflected in all arrow heads. */
	val triangle: Triangle = {
		val pt1 = Vector2D(0, -1 * base / 2)
		val pt2 = Vector2D(0, base / 2)
		val pt3 = Vector2D(height, 0)
		Triangle(pt1, pt2, pt3)
			.scaled(scaleFactor)
	}
}

object ArrowRenderProperties {
	// ratio of 5:7 for triangle makes it roughly equilateral
	// the actual numbers and the scale factor are arbitrary from experimentation, should probably be abstracted away better into
	// a "unit" triangle that can be scaled based on node size
	private val defaultBase = 10
	private val defaultHeight = 14
	private val scaleFactor = 2
	private val arrowPadding = 4 // empty space for how far arrow is moved back from the end of the edge of the node (see diagram above)
	private val defaultColor = "#32BFE3"
	private def default(nodeRadius: Int): ArrowRenderProperties = {
		val displacement = nodeRadius + arrowPadding + (defaultHeight * scaleFactor)
		ArrowRenderProperties(defaultHeight, defaultBase, 2, displacement, defaultColor)
	}

	val default: ArrowRenderProperties = default(NodeRenderProperties.default.nodeRadius)
}