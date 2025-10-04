package scalagraph.render

import scalagraph.dataobject.{Edge, Point}
import scalagraph.render.properties.ArrowRenderProperties
import scalagraph.dataobject.canvas.TriangleCanvas

object ArrowTipRender {

	/** Given the list of (directed) edges, determines where to put the arrow (i.e. the Triangle).
	 *                  from -------> to
	 * It will put the arrow head pointing at and closes to the 'to' point of the Edge case class. So a bidirectional edge
	 * would need to have both (a, b) and (b, a) passed as inputs in order for there to be two arrows.
	 *
	 * TODO: Possibly this function could take a DirectedEdge like elsewhere in the code instead of passing in double edges*/
	def getTriangles(edges: Seq[Edge]): Seq[TriangleCanvas] = {
		val arrowProperties = ArrowRenderProperties.default

		edges.map { e =>
			val dx = e.to.x - e.from.x
			val dy = e.to.y - e.from.y

			// rotate the triangle to match the edge angle
			val rotate_radians = math.atan2(dy, dx); // angle in radians
			val rotatedTriangle = arrowProperties.triangle.rotate(rotate_radians)

			// find the vector of edge endpoint ("to") and moves the triangle back along that vector by the displacement amount
			// this keeps the arrow tip from overlapping the node circle
			val edgeLength = math.sqrt(dx * dx + dy * dy)
			val ratio = arrowProperties.displacement / edgeLength
			val dxScaled = dx * ratio
			val dyScaled = dy * ratio
			val translateEndPoint = Point((-1 * dxScaled + e.to.x).toInt, (-1 * dyScaled + e.to.y).toInt)

			val finalTriangle = rotatedTriangle.translate(translateEndPoint)

			TriangleCanvas(
				tri = finalTriangle,
				color = arrowProperties.color
			)
		}
	}
}
