package scalagraph.render

import scalagraph.dataobject.{Edge, Point}
import scalagraph.render.properties.ArrowRenderProperties
import scalagraph.dataobject.canvas.TriangleCanvas

/*
const triangleHeight = 14;
const triangleBase = 10;
const tri_1 = new Point(0, -1 * triangleBase / 2);
const tri_2 = new Point(0, triangleBase / 2);
const tri_3 = new Point(triangleHeight, 0);
const originTriangle = [tri_1, tri_2, tri_3];
// scaling
const scale_factor = 2;;
const trisScaledOrigin = originTriangle.map(pt => new Point(pt.x * scale_factor, pt.y * scale_factor));
 */

object ArrowTipRender {

	def getTriangles(edges: Seq[Edge]): Seq[TriangleCanvas] = {
		val arrowProperties = ArrowRenderProperties.default

		// TODO finish this and delete Seq.empty a the bottom
		edges.map { e =>
			val dx = e.to.x - e.from.x
			val dy = e.to.y - e.from.y

			val rotate_radians = math.atan2(dy, dx); // angle in radians
			val rotatedTriangle = arrowProperties.triangle.rotate(rotate_radians)

			val edgeLength = math.sqrt(dx * dx + dy * dy)
			val ratio = arrowProperties.displacement / edgeLength
			val dxScaled = dx * ratio
			val dyScaled = dy * ratio
			val translateEndPoint = Point((-1 * dxScaled + e.to.x).toInt, (-1 * dyScaled + e.to.y).toInt)

			val finalTriangle = rotatedTriangle.translate(translateEndPoint)
			TriangleCanvas(finalTriangle, arrowProperties.color)
		/*
				const edgeLength = Math.sqrt(dx * dx + dy * dy);
				const ratio = arrowDisplacement / edgeLength;
				const dxFromEndNode = dx * ratio;
				const dyFromEndNode = dy * ratio;
				const translateVec = new Point(
					-1 * dxFromEndNode + e.to.x,
					-1 * dyFromEndNode + e.to.y
				);
				tris = tris.map(pt => new Point(pt.x + translateVec.x, pt.y + translateVec.y));

				// floor values so we only pass integers to the canvas
				// (recommended by canvas docs)
				tris = tris.map(pt => new Point(Math.floor(pt.x), Math.floor(pt.y)));

				arrowRenderCache.set(key, tris);
			 */
		}
	}
}
