package graphcontroller.render

import graphcontroller.dataobject.{Line, Point}
import graphcontroller.render.properties.ArrowRenderProperties
import graphcontroller.dataobject.canvas.TriangleCanvas

object ArrowTipRender {
	private val arrowProperties = ArrowRenderProperties.default

	/** Given the Line (i.e. the edge), determines where to put the arrow (i.e. the Triangle).
	 *                  from -------> to
	 * It will put the arrow head pointing at and closest to the 'to' point of the Line case class. So a bidirectional edge
	 * would need to have both (a, b) and (b, a) passed as inputs in order for there to be two arrows.
	 */
	def getArrowTriangle(e: Line, props: ArrowRenderProperties): TriangleCanvas = {
		val dx = e.to.x - e.from.x
		val dy = e.to.y - e.from.y

		// rotate the triangle to match the edge angle
		val rotate_radians = math.atan2(dy, dx); // angle in radians
		val rotatedTriangle = props.triangle.rotate(rotate_radians)

		// find the vector of edge endpoint ("to") and moves the triangle back along that vector by the displacement amount
		// this keeps the arrow tip from overlapping the node circle
		val edgeLength = math.sqrt(dx * dx + dy * dy)
		val ratio = props.displacement / edgeLength
		val dxScaled = dx * ratio
		val dyScaled = dy * ratio
		val translateEndPoint = Point((-1 * dxScaled + e.to.x).toInt, (-1 * dyScaled + e.to.y).toInt)

		val finalTriangle = rotatedTriangle.translate(translateEndPoint)

		TriangleCanvas(
			tri = finalTriangle,
			color = props.color
		)
	}

	def getTriangles(edges: Seq[Line]): Seq[TriangleCanvas] = edges.map(getArrowTriangle(_, arrowProperties))
}
