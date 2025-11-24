let matrixElem = document.getElementById("adj-matrix");

const adjMatrix = [
    [true, false, true],
    [true, false, false],
    [false, true, true]
];

// Global flags and colors
const COLORS = {
    cellFill: "#cff5ff",
    cellActive: "orange",
    cellDefault: "black",
    nodeLabel: "orange",
    border: "red"
};
let DRAW_CELL_BORDER_SINGLE = true;
let DRAW_CELL_BORDER_ROWCOL = true;
// Add global flag for row/column highlight
let DRAW_ROW_COL_HIGHLIGHT = true;

function drawCells(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn, highlightCells = []) {
    // Convert highlightCells to a Set for fast lookup
    const highlightSet = new Set(highlightCells.map(([r, c]) => `${r},${c}`));
    for (let i = 0; i < adjMatrix.length; i++) {
        for (let j = 0; j < adjMatrix[i].length; j++) {
            let isHighlighted = highlightSet.has(`${j},${i}`);
            if (adjMatrix[i][j]) {
                if ((i == hoverColumn && j == hoverRow) || isHighlighted) {
                    ctx.fillStyle = COLORS.cellActive;
                } else {
                    ctx.fillStyle = COLORS.cellDefault;
                }
                ctx.fillRect(cellWidth * (i + 1), cellHeight * (j + 1), cellWidth, cellHeight);
                if ((i == hoverColumn && j == hoverRow) || isHighlighted) ctx.fillStyle = COLORS.cellDefault;
            } else if ((i == hoverColumn && j == hoverRow) || isHighlighted) {
                ctx.fillStyle = COLORS.cellFill;
                ctx.fillRect(cellWidth * (i + 1), cellHeight * (j + 1), cellWidth, cellHeight);
                ctx.fillStyle = COLORS.cellDefault;
            }
        }
    }
}

function drawNodeLabels(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn) {
    ctx.font = "0.8rem Arial";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = COLORS.nodeLabel;
    for (let i = 0; i < adjMatrix.length; i++) {
        const label = i;
        const ADJUSTMENT = 1.5;
        if (i == hoverRow) ctx.fillText(label, cellWidth / 2, cellHeight * (i + 1.5) + ADJUSTMENT);
        if (i == hoverColumn) ctx.fillText(label, cellWidth * (i + 1.5), cellHeight / 2 + ADJUSTMENT);
    }
}

function drawCellBorders(ctx, cells, cellWidth, cellHeight, numNodes) {
    //if (!DRAW_CELL_BORDER_SINGLE) return;
    ctx.strokeStyle = COLORS.border;
    ctx.lineWidth = 2;
    const cellSet = new Set(cells.map(([r, c]) => `${r},${c}`));
    const offset = 1; // 1px offset for outer border
    for (const [row, col] of cells) {
        if (
            row < 0 || row >= numNodes ||
            col < 0 || col >= numNodes
        ) continue;
        const x = cellWidth * (col + 1);
        const y = cellHeight * (row + 1);
        // Top edge
        if (!cellSet.has(`${row-1},${col}`)) {
            ctx.beginPath();
            ctx.moveTo(x - offset, y - offset);
            ctx.lineTo(x + cellWidth + offset, y - offset);
            ctx.stroke();
        }
        // Right edge
        if (!cellSet.has(`${row},${col+1}`)) {
            ctx.beginPath();
            ctx.moveTo(x + cellWidth + offset, y - offset);
            ctx.lineTo(x + cellWidth + offset, y + cellHeight + offset);
            ctx.stroke();
        }
        // Bottom edge
        if (!cellSet.has(`${row+1},${col}`)) {
            ctx.beginPath();
            ctx.moveTo(x + cellWidth + offset, y + cellHeight + offset);
            ctx.lineTo(x - offset, y + cellHeight + offset);
            ctx.stroke();
        }
        // Left edge
        if (!cellSet.has(`${row},${col-1}`)) {
            ctx.beginPath();
            ctx.moveTo(x - offset, y + cellHeight + offset);
            ctx.lineTo(x - offset, y - offset);
            ctx.stroke();
        }
    }
}

function drawAdjacencyMatrix(adjMatrix, hoverRow, hoverColumn, highlightRowCol = false) {
    if (matrixElem && matrixElem.getContext) {
        // calculate dimensions, and clear rectangle
        let totalWidth = matrixElem.offsetWidth;
        let totalHeight = matrixElem.offsetHeight;
        let ctx = matrixElem.getContext("2d");
        ctx.clearRect(0, 0, totalWidth, totalHeight);

        ctx.fillStyle = "black";

        const cellWidth = totalWidth / (adjMatrix.length + 2); // +2 for padding for node labels
        const cellHeight = totalHeight / (adjMatrix.length + 2); // +2 for padding for node labels
        let highlightCells = [];
        let drawBorderFlag = false;
        if (highlightRowCol && DRAW_ROW_COL_HIGHLIGHT) {
            drawBorderFlag = DRAW_CELL_BORDER_ROWCOL;
            if (hoverRow !== null && hoverRow >= 0 && hoverRow < adjMatrix.length) {
                for (let col = 0; col < adjMatrix.length; col++) {
                    highlightCells.push([hoverRow, col]);
                }
            } else if (hoverColumn !== null && hoverColumn >= 0 && hoverColumn < adjMatrix.length) {
                for (let row = 0; row < adjMatrix.length; row++) {
                    highlightCells.push([row, hoverColumn]);
                }
            }
        } else if (hoverRow !== null && hoverColumn !== null && hoverRow >= 0 && hoverRow < adjMatrix.length && hoverColumn >= 0 && hoverColumn < adjMatrix.length) {
            highlightCells = [[hoverRow, hoverColumn]];
            drawBorderFlag = DRAW_CELL_BORDER_SINGLE;
        }
        drawCells(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn, highlightCells);
        // Only draw node labels if not hovering over right/bottom label areas
        let showLabels = true;
        if (hoverRow !== null && hoverRow >= adjMatrix.length) showLabels = false;
        if (hoverColumn !== null && hoverColumn >= adjMatrix.length) showLabels = false;
        if (showLabels) {
            drawNodeLabels(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn);
        }
        // Draw border around highlighted cells
        if (highlightCells.length > 0 && drawBorderFlag) {
            drawCellBorders(ctx, highlightCells, cellWidth, cellHeight, adjMatrix.length);
        }
    }
}

drawAdjacencyMatrix(adjMatrix, null, null);

// any way to avoid mutable state?
var col = null;
var row = null;


if (matrixElem) {
    matrixElem.addEventListener("mousemove", function(event) {
        const rect = matrixElem.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        let totalWidth = matrixElem.offsetWidth;
        let totalHeight = matrixElem.offsetHeight;
        const cellWidth = totalWidth / (adjMatrix.length + 2); // +2 for padding for node labels
        const cellHeight = totalHeight / (adjMatrix.length + 2); // +2 for padding for node labels
        let _col = Math.floor((x - cellWidth) / cellWidth);
        let _row = Math.floor((y - cellHeight) / cellHeight);
        col = _col;
        row = _row;
        // Determine if hovering over top/left label area
        let highlightRowCol = false;
        if (x >= 0 && x < cellWidth) {
            // left label area
            highlightRowCol = true;
            _col = null;
        } else if (y >= 0 && y < cellHeight) {
            // top label area
            highlightRowCol = true;
            _row = null;
        }
        // If hovering over right/bottom label area, do not highlight or show labels
        if (x > cellWidth * (adjMatrix.length + 1) || y > cellHeight * (adjMatrix.length + 1)) {
            drawAdjacencyMatrix(adjMatrix, _row, _col, false);
        } else {
            drawAdjacencyMatrix(adjMatrix, _row, _col, highlightRowCol);
        }
    });

    matrixElem.addEventListener("mouseleave", function(event) {
        drawAdjacencyMatrix(adjMatrix, null, null);
    });

    matrixElem.addEventListener("mousedown", function(event) {
        console.log("mousedown on (" + col + ", " + row + ")");
        console.log("mousedown");
    });
    matrixElem.addEventListener("mouseup", function(event) {
        console.log("mouseup on (" + col + ", " + row + ")");
        console.log("mouseup");
    });
}