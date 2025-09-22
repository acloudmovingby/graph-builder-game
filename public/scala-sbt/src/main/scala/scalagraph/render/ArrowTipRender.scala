package scalagraph.render

import scalagraph.dataobject.Edge
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
		val properties = ArrowRenderProperties.default

		// TODO finish this and delete Seq.empty a the bottom
		edges.map { e =>
			val dx = e.to.x - e.from.x
			val dy = e.to.y - e.from.y

			val rotate_radians = math.atan2(dy, dx); // angle in radians
			val rotatedTriangle = properties.triangle.rotate(rotate_radians)

			val edgeLength = math.sqrt(dx * dx + dy * dy)

			()
		/*
			// rotate
				const dx = e.to.x - e.from.x;
				const dy = e.to.y - e.from.y;
				const rotate_radians = Math.atan2(dy, dx); // angle in radians
				const rotateMatrix = [[Math.cos(rotate_radians), -Math.sin(rotate_radians)], [Math.sin(rotate_radians), Math.cos(rotate_radians)]];
				tris = tris.map(pt => {
					const rotatedX = pt.x * rotateMatrix[0][0] + pt.y * rotateMatrix[0][1];
					const rotatedY = pt.x * rotateMatrix[1][0] + pt.y * rotateMatrix[1][1];
					return new Point(rotatedX, rotatedY);
				});

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
		Seq.empty
	}
}
