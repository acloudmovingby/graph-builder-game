// =====================
// Rendering Functions
// =====================
// Functions to draw basic shapes to the canvas

// lines is an array of CanvasLineJS
export function drawLines(ctx, lines) {
//     ctx.lineWidth = simpleEdgeStrokeWidth;
//     ctx.strokeStyle = simpleEdgeStrokeColor;
    ctx.beginPath();
    lines.forEach((e) => {
        ctx.lineWidth = e.width;
        ctx.strokeStyle = e.color;
        ctx.moveTo(e.from.x, e.from.y);
        ctx.lineTo(e.to.x, e.to.y);
    });
    ctx.stroke();
}

// edges is an array of the CanvasLineJS class
export function drawTriangles(ctx, triangles) {
    triangles.forEach((triObject) => {
        // triObject is a TriangleCanvasJS object
        ctx.setLineDash([]); // reset line dash to solid/normal TODO: make reset function for between every different shape section
        ctx.strokeStyle = triObject.color;
        ctx.fillStyle = triObject.color;
        ctx.beginPath();
        ctx.moveTo(triObject.tri.pt1.x, triObject.tri.pt1.y);
        ctx.lineTo(triObject.tri.pt2.x, triObject.tri.pt2.y);
        ctx.lineTo(triObject.tri.pt3.x, triObject.tri.pt3.y);
        ctx.closePath();
        ctx.fill();
    });
}