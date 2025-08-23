// =====================
// CONSTANTS
// =====================
// Parameters for styling the rendered shapes on the canvas
export const nodeRadius = 15;

//// Arrow
// Base triangle, placed at the origin, top pointing to the right
export function Point(x, y) {
  this.x = x;
  this.y = y;
}


// =====================
// Rendering Functions
// =====================
// The point of these functions is to isolate the canvas rendering code from the application logic.

export function drawNodes(ctx, nodes) {
    throw new Error("Function not implemented.");
}