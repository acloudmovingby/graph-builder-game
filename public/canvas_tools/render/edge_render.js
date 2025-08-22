// =====================
// CONSTANTS
// =====================
// Parameters for styling the rendered shapes on the canvas
const nodeRadius = 15;
const simpleEdgeStrokeColor = "orange";
const simpleEdgeStrokeWidth = 8;


//// Arrow
// Base triangle, placed at the origin, top pointing to the right
function Point(x, y) {
  this.x = x;
  this.y = y;
}
const triangleHeight = 9;
const triangleBase = 10;
const tri_1 = new Point(0, -1 * triangleBase / 2);
const tri_2 = new Point(0, triangleBase / 2);
const tri_3 = new Point(triangleHeight, 0);
const originTriangle = [tri_1, tri_2, tri_3];
// scaling
const scale_factor = 2;;
const trisScaledOrigin = originTriangle.map(pt => new Point(pt.x * scale_factor, pt.y * scale_factor));
// how far back the arrow is moved from the end of the edge
const arrowPadding = 4; // how far arrow is moved back from the end of the edge of the node
const arrowDisplacement = nodeRadius + (triangleHeight * scale_factor) + arrowPadding;


// =====================
// Rendering Functions
// =====================
// The point of these functions is to isolate the canvas rendering code from the application logic.

// edges is an array of 4 integer arrays, i.e. [x1, y1, x2, y2]
function drawSimpleEdges(ctx, edges) {
    // This method is called to render the edge on the canvas
    ctx.lineWidth = simpleEdgeStrokeWidth;
    ctx.strokeStyle = simpleEdgeStrokeColor;
    ctx.beginPath();
    edges.forEach((e) => {
        ctx.moveTo(e[0], e[1]);
        ctx.lineTo(e[2], e[3]);
    });
    ctx.stroke();
}

const arrowRenderCache = new Map(); // map from "x1,y1,x2,y2" to pre-calculated points

// edges is an array of 4 integer arrays, i.e. [[x1, y1, x2, y2], ...]. Each inner array is an edge represented by its start and end coordinates.
function drawDirectedEdges(ctx, edges) {
    // TODO replace this with new function that draws edges terminating at the base of the arrow
    drawSimpleEdges(ctx, edges);
    // draw triangle (arrow) offset from end of each edge and rotated to match angle of edge
    ctx.beginPath();
    edges.forEach((e) => {
        let key = `${e[0]},${e[1]},${e[2]},${e[3]}`;
        let tris = trisScaledOrigin;

        if (!arrowRenderCache.has(key)) {
            // rotate
            const dx = e[2] - e[0];
            const dy = e[3] - e[1];
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
                -1 * dxFromEndNode + e[2],
                -1 * dyFromEndNode + e[3]
            );
            tris = tris.map(pt => new Point(pt.x + translateVec.x, pt.y + translateVec.y));

            // floor values so we only pass integers to the canvas
            // (recommended by canvas docs)
            tris = tris.map(pt => new Point(Math.floor(pt.x), Math.floor(pt.y)));

            arrowRenderCache.set(key, tris);
        }

        tris = arrowRenderCache.get(key);

        // style
        ctx.setLineDash([]); // reset line dash to solid/normal TODO: make reset function for between every different shape section
        ctx.strokeStyle = "#32BFE3";
        ctx.fillStyle = "#32BFE3";

        // draw

        ctx.moveTo(tris[0].x, tris[0].y);
        ctx.lineTo(tris[1].x, tris[1].y);
        ctx.lineTo(tris[2].x, tris[2].y);
    });
    ctx.fill();
}

exports.drawSimpleEdges = drawSimpleEdges;
exports.drawDirectedEdges = drawDirectedEdges;