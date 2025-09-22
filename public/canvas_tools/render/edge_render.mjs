import { nodeRadius } from './node_render.mjs';

// =====================
// CONSTANTS
// =====================
// Parameters for styling the rendered shapes on the canvas
const simpleEdgeStrokeColor = "orange";
const simpleEdgeStrokeWidth = 8;


//// Arrow
// Base triangle, placed at the origin, top pointing to the right
export function Point(x, y) {
  this.x = x;
  this.y = y;
}
const triangleHeight = 14;
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
// These isolate the canvas rendering code from the graph/application logic.
// They take the minimal graph data and calculate the points to draw, then call the canvas API to draw the shapes.

// edges is an array of 4 integer arrays, i.e. [x1, y1, x2, y2]
export function drawSimpleEdges(ctx, edges) {
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

// I'm calling this function twice for directed edges, but perhaps this should just take a 'bidirectional' boolean parameter and
// go ahead and trim both ends if true
function trimEdges(trimStart, edges) {
    const trimmedEdges = edges.map(e => {
            const dx = e[2] - e[0];
            const dy = e[3] - e[1];
            const edgeLength = Math.sqrt(dx * dx + dy * dy);
            // move start point to edge of node, -1 to avoid gap between edge and arrow due to anti-aliasing
            const ratio = (arrowDisplacement - 1) / edgeLength;
            const dxFromNode = dx * ratio;
            const dyFromNode = dy * ratio;

            if (trimStart) {
                return [
                    Math.floor(e[0] + dxFromNode),
                    Math.floor(e[1] + dyFromNode),
                    Math.floor(e[2]),
                    Math.floor(e[3])
                ];
            } else {
                return [
                    Math.floor(e[0]),
                    Math.floor(e[1]),
                    Math.floor(e[2] - dxFromNode),
                    Math.floor(e[3] - dyFromNode)
                ];
            }


        });
    return trimmedEdges;
    }

function DirectedEdge(bidirectional, startX, startY, endX, endY) {
    // bidirectional is boolean
    this.bidirectional = bidirectional;
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
}

function decideDirectionality(edges) {
    // make list of DirectedEdge objects where if both directions are an edge are represented in the input edges, the DirectedEdge is bidirectional
    const directedEdges = [];
    const edgeSet = new Set(edges.map(e => `${e[0]},${e[1]},${e[2]},${e[3]}`));
    const seen = new Set();
    edges.forEach(e => {
        const key = `${e[0]},${e[1]},${e[2]},${e[3]}`;
        const reverseKey = `${e[2]},${e[3]},${e[0]},${e[1]}`;
        if (!seen.has(key) && !seen.has(reverseKey)) {
            const bidirectional = edgeSet.has(reverseKey);
            directedEdges.push(new DirectedEdge(bidirectional, e[0], e[1], e[2], e[3]));
            seen.add(key);
            seen.add(reverseKey);
        }
    });
    return directedEdges;
}

function trimEdgesBasedOnDirectionality(directedEdges) {
    const trimmedEdges = directedEdges.map(de => {
        if (de.bidirectional) {
            // trim both ends
            let frontTrimmed = trimEdges(true, [[de.startX, de.startY, de.endX, de.endY]])[0];
            return trimEdges(false, [frontTrimmed])[0];
        } else {
            // trim only end
            return trimEdges(false, [[de.startX, de.startY, de.endX, de.endY]])[0];
        }
    });
    return trimmedEdges;
}

// edges is an array of 4 integer arrays, i.e. [[x1, y1, x2, y2], ...]. Each inner array is an edge represented by its start and end coordinates.
// TODO change edges to be the CanvasLine class
export function drawDirectedEdges(ctx, edges, triangles) {
    edges = edges.map((e) => [e.from.x, e.from.y, e.to.x, e.to.y]);
    // draw edges terminating at the base of the arrow (arrowDisplacement away from center of target node)
    // determine directionality of each edge
    const directedEdges = decideDirectionality(edges);
    const trimmedEdges = trimEdgesBasedOnDirectionality(directedEdges);
    drawSimpleEdges(ctx, trimmedEdges);

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