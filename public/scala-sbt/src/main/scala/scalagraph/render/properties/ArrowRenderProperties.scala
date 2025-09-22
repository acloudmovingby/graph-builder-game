package scalagraph.render.properties

import scalagraph.dataobject.{Point, Triangle}

case class ArrowRenderProperties(
	height: Int,
	base: Int,
	scaleFactor: Int,
	displacement: Int,
	color: String
) {
	val triangle: Triangle = {
		val pt1 = Point(0, -1 * base / 2)
		val pt2 = Point(0, base / 2)
		val pt3 = Point(height, 0)
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
	private val arrowPadding = 4 // how far arrow is moved back from the end of the edge of the node
	private val defaultColor = "orange"
	private def default(nodeRadius: Int): ArrowRenderProperties = {
		/* displacement is based on node radius + padding + triangle height

		      |  .  |  <- node center
		 	   \___/   <- edge of node
		 	     |
		         | <- arrowPadding
		         |
		        /\    |
		       /  \   | <- triangle height
		      /____\  |
		         |
		*/
		val displacement = nodeRadius + arrowPadding + (defaultHeight * scaleFactor)
		ArrowRenderProperties(defaultHeight, defaultBase, 2, displacement, defaultColor)
	}

	val default: ArrowRenderProperties = default(NodeRenderProperties.default.nodeRadius)
}