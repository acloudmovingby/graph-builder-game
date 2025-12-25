// =====================
// Rendering Functions
// =====================
// Functions to draw basic shapes to the canvas

// lines is an array of CanvasLineJS
export function drawLines(ctx, lines) {
    ctx.setLineDash([]); // reset line dash to solid/normal TODO: make reset function for between every different shape section
    lines.forEach((e) => {
        ctx.beginPath();
        ctx.lineWidth = e.width;
        ctx.strokeStyle = e.color;
        ctx.moveTo(e.from.x, e.from.y);
        ctx.lineTo(e.to.x, e.to.y);
        ctx.stroke();
    });
}

// triangles is an array of TriangleCanvasJS
export function drawTriangles(ctx, triangles) {

    ctx.setLineDash([]); // reset line dash to solid/normal TODO: make reset function for between every different shape section
    triangles.forEach((triObject) => {
        ctx.beginPath();
        ctx.strokeStyle = triObject.color;
        ctx.fillStyle = triObject.color;
        ctx.moveTo(triObject.tri.pt1.x, triObject.tri.pt1.y);
        ctx.lineTo(triObject.tri.pt2.x, triObject.tri.pt2.y);
        ctx.lineTo(triObject.tri.pt3.x, triObject.tri.pt3.y);
        ctx.fill();
    });
}