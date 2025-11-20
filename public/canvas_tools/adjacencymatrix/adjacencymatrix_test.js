let matrixElem = document.getElementById("adj-matrix");

const adjMatrix = [
    [true, false, true],
    [true, false, false],
    [false, true, true]
];

function drawCells(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn) {
    for (let i = 0; i < adjMatrix.length; i++) {
        for (let j = 0; j < adjMatrix[i].length; j++) {
            if (adjMatrix[i][j]) {
                if (i == hoverColumn && j == hoverRow) ctx.fillStyle = "orange";
                ctx.fillRect(cellWidth * (i + 1), cellHeight * (j + 1), cellWidth, cellHeight);
                if (i == hoverColumn && j == hoverRow) ctx.fillStyle = "black";
            } else if (i == hoverColumn && j == hoverRow) {
                ctx.fillStyle = "#cff5ff";
                ctx.fillRect(cellWidth * (i + 1), cellHeight * (j + 1), cellWidth, cellHeight);
                ctx.fillStyle = "black";
            }
        }
    }
}

function drawNodeLabels(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn) {
    ctx.font = "0.8rem Arial";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = "orange";
    for (let i = 0; i < adjMatrix.length; i++) {
        const label = i;
        const ADJUSTMENT = 1.5;
        if (i == hoverRow) ctx.fillText(label, cellWidth / 2, cellHeight * (i + 1.5) + ADJUSTMENT);
        if (i == hoverColumn) ctx.fillText(label, cellWidth * (i + 1.5), cellHeight / 2 + ADJUSTMENT);
    }
}

function drawAdjacencyMatrix(adjMatrix, hoverRow, hoverColumn) {
    if (matrixElem && matrixElem.getContext) {
        // calculate dimensions, and clear rectangle
        let totalWidth = matrixElem.offsetWidth;
        let totalHeight = matrixElem.offsetHeight;
        let ctx = matrixElem.getContext("2d");
        ctx.clearRect(0, 0, totalWidth, totalHeight);

        ctx.fillStyle = "black";

        const cellWidth = totalWidth / (adjMatrix.length + 2); // +2 for padding for node labels
        const cellHeight = totalHeight / (adjMatrix.length + 2); // +2 for padding for node labels

        drawCells(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn);
        drawNodeLabels(ctx, adjMatrix, cellWidth, cellHeight, hoverRow, hoverColumn);
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
        const n = matrixElem.width; // number of pixels, not nodes
        // Get node count from Scala

        // by defining these in the event listener it means it's not responsive to screen size changes...but whatever...
        let totalWidth = matrixElem.offsetWidth;
        let totalHeight = matrixElem.offsetHeight;

        const cellWidth = totalWidth / (adjMatrix.length + 2); // +2 for padding for node labels
        const cellHeight = totalHeight / (adjMatrix.length + 2); // +2 for padding for node labels

        const _col = Math.floor((x - cellWidth) / cellWidth);
        const _row = Math.floor((y - cellHeight) / cellHeight);
        col = _col;
        row = _row;

        console.log("hovering over (" + _col + ", " + _row + ")");
        drawAdjacencyMatrix(adjMatrix, _row, _col);
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