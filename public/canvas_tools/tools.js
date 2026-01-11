import {
    Graph,
    Digraph
} from "../algorithms/graph.mjs";
import {
    calculateGraphType,
    getDot
} from "../algorithms/graph_algs.mjs";

// =====================
// Class/Type Definitions
// =====================
class ToolTipHover {
    constructor(header, description, image) {
        this.header = header;
        this.description = description;
        this.image = image;
    }
}

class Tool {
    constructor(id, cursor, toolTipHover, state) {
        this.id = id; // html id
        this.cursor = cursor; // css for url of cursor image
        this.hover = toolTipHover;
        this.state = state; // varies by tool. This cuts down on global variables. every tool is responsible for managing the state it requires (e.g. the prior node clicked for an edge adding tool, an array of nodes selected by a selection tool, etc.);
    }
}

function NodeData(key, counter, x, y) {
    this.key = key;
    this.counter = counter;
    this.x = x;
    this.y = y;
}

function cloneNodeData(nodeData) {
    return new NodeData(nodeData.key, nodeData.counter, nodeData.x, nodeData.y);
}

function Point(x, y) {
    this.x = x;
    this.y = y;
}

// =====================
// State and Constants
// =====================
const nodeRadius = 18;
let canvas = document.getElementById("canvas");
let canvasArea = document.getElementById("canvas-area");
const infoPaneWidth = document.getElementsByClassName("right-pane")?.[0].offsetWidth;
let graph = new Digraph();
const graphController = Main.getGraphController();
let mouseX = 0;
let mouseY = 0;
let nodeHover = null;
let infoPaneHover = false;
let labelsVisible = true;
const timeInit = new Date().getSeconds();
let printCounter = 0;
let scale = window.devicePixelRatio;
let graphTypes = [];


// =====================
// Tool Definitions
// =====================
let basicTool = new Tool(
    "basic",
    "url('images/pointer.svg'), pointer",
    new ToolTipHover(
        "Basic Node/Edge Adding Tool",
        "Click to make nodes, then click on a node to begin adding edges. To exit edge making mode, simply click on the gray canvas.",
        "images/basic-tool-tooltip-example.gif"
    ), {
        edgeMode: false,
        edgeStart: null, // the node's key
        stillInNode: false,
    }
);

let areaCompleteTool = new Tool(
    "area-complete",
    "url('images/area-complete-cursor.svg'), pointer",
    new ToolTipHover(
        "Area Complete Tool",
        "Adds all possible edges between nodes in the selected area.",
        "images/area-complete-tool-tooltip-example.gif"
    ), {
        mousePressed: false,
        drawPoints: [],
    }
);

let magicPathTool = new Tool(
    "magic-path",
    "url('images/magic-path-cursor-2.svg'), pointer",
    new ToolTipHover(
        "Magic Path Tool",
        "Click on a node then simply move the mouse to other nodes to automatically build a path! No need to drag or click. Magic!",
        "images/magic-path-tool-tooltip-example.gif"
    ), {
        edgeMode: false,
        edgeStart: null, // the node's key
        normalCursor: "url('images/magic-path-cursor-2.svg'), pointer",
        noneCursor: "none",
    }
);

let moveTool = new Tool(
    "move",
    "url('images/move-tool-cursor.svg'), pointer",
    new ToolTipHover(
        "Move Tool",
        "Click and drag it around.",
        "images/move-tool-tooltip-example.gif"
    ), {
        node: null,
    }
);

const toolState = {
    curTool: basicTool,
    allTools: [basicTool, areaCompleteTool, magicPathTool, moveTool],
};

// =====================
// Canvas Setup
// =====================
// TODO: Things don't work if I don't do this, but I suspect if I set up the css properly, this shouldn't be necessary?
// (except for the scale issue, I'm not sure that can be solved with css alone)
// TODO: Also let's combine this with (1) the resize listener and maybe (2) adj-matrix canvas set up as well
function setCanvasSize() {
    const canvasWidth = window.innerWidth - infoPaneWidth;
    const canvasHeight = window.innerHeight;
    canvas.style.width = canvasWidth + "px";
    canvas.style.height = canvasHeight + "px";

    // Set actual canvas size to scaled size for high-DPI displays (keeps edges looking sharp)
    canvas.width = canvasWidth * scale;
    canvas.height = canvasHeight * scale;
    if (canvas.getContext) {
        let ctx = canvas.getContext("2d");
        ctx.scale(scale, scale);
    }
}
setCanvasSize();

const adjMatrixElem = document.getElementById("adj-matrix");
if (adjMatrixElem) {
    adjMatrixElem.width = adjMatrixElem.offsetWidth * scale;
    adjMatrixElem.height = adjMatrixElem.offsetHeight * scale;
    adjMatrixElem.style.width = adjMatrixElem.offsetWidth + "px";
    adjMatrixElem.style.height = adjMatrixElem.offsetHeight + "px";
    if (adjMatrixElem.getContext) {
        let ctx = adjMatrixElem.getContext("2d");
        ctx.scale(scale, scale);
    }
}

// =====================
// Event Listeners
// =====================
window.addEventListener('resize', function(event) {
    setCanvasSize()
});

for (const tool of toolState.allTools) {
    if (document.getElementById(tool.id)) {
        document.getElementById(tool.id).addEventListener(
            "click",
            () => {
                toolState.curTool = tool;
                refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
            },
            false
        );
    }
}

for (const tool of toolState.allTools) {
    if (document.getElementById(tool.id)) {
        document.getElementById(tool.id).addEventListener(
            "mouseenter",
            (event) => {
                let hoverInfoElement = document.getElementById("hover-info-pane");
                if (hoverInfoElement) {
                    let toolBtnOffsetLeft = document.getElementById(tool.id).offsetLeft;
                    let toolBtnWidth = document.getElementById(tool.id).offsetWidth;
                    let toolBtnHeight = document.getElementById(tool.id).offsetHeight;
                    hoverInfoElement.style.left = `${toolBtnOffsetLeft + toolBtnWidth / 2}px`;
                    hoverInfoElement.style.top = `${toolBtnHeight - 5}px`;
                    hoverInfoElement.style.visibility = "visible";
                    document.getElementById("hover-header").innerHTML = tool.hover.header;
                    document.getElementById("hover-description").innerHTML = tool.hover.description;
                    document.getElementById("hover-info-img").src = tool.hover.image;
                }
            },
            false
        );
        document.getElementById(tool.id).addEventListener(
            "mouseleave",
            (event) => {
                let hoverInfoElement = document.getElementById("hover-info-pane");
                if (hoverInfoElement) {
                    hoverInfoElement.style.visibility = "hidden";
                }
            },
            false
        );
    }
}

document.onkeydown = function(event) {
    event = event || window.event;
    var isEscape = false;
    if ("key" in event) {
        isEscape = event.key === "Escape" || event.key === "Esc";
    }
    if (isEscape) {
        if (toolState.curTool === basicTool) exitBasicEdgeMode();
        if (toolState.curTool === magicPathTool) exitMagicEdgeMode();
    }
};

canvasArea.style.cursor = toolState.curTool.cursor;

let undoElem = document.getElementById("undo");
if (undoElem) {
    undoElem.addEventListener("click", undo, false);
}

if (canvas.getContext) {
    canvasArea.addEventListener("mousedown", mouseDown, false);
    canvasArea.addEventListener("mousemove", mouseMove, false);
    canvasArea.addEventListener("mouseleave", mouseLeave, false);
    canvasArea.addEventListener("mouseup", mouseUp, false);
    window.requestAnimationFrame(draw);
}

// =====================
// Main Drawing and Core Logic
// =====================
function draw() {
    if (canvas.getContext) {
        let ctx = canvas.getContext("2d");

        ctx.clearRect(0, 0, window.innerWidth * 2, window.innerHeight * 2);
        ctx.setLineDash([])

        const welcome = document.getElementById('welcome-message');
        welcome.style.visibility = graphController.nodeCount() === 0 ? "visible" : "hidden";

        //edge mode, draw edge from edgeStart to mouse cursor
        let inBasicEdgeMode =
            toolState.curTool === basicTool && basicTool.state.edgeMode;
        let inMagicPathEdgeMode =
            toolState.curTool === magicPathTool && magicPathTool.state.edgeMode;
        if (inBasicEdgeMode || inMagicPathEdgeMode) {
            ctx.beginPath();
            ctx.lineWidth = 8;
            ctx.strokeStyle = "#ffdc7a";
            let edgeStart = toolState.curTool.state.edgeStart;
            let data = graphController.getNodeData(edgeStart);
            ctx.moveTo(data.x, data.y);
            ctx.lineTo(mouseX, mouseY);
            ctx.closePath();
            ctx.stroke();
        }

        // trigger drawing of edge shapes in the ScalaJS code
        const shapes = graphController.renderMainCanvas();

        // It's beautiful (the clouds)
        // like you

        // draw nodes
        let nodes = graphController.getFullNodeData();
        for (let i = 0; i < nodes.length; i++) {
            const isEdgeStart = nodes[i].key === toolState.curTool.state.edgeStart;
            ctx.beginPath();
            ctx.lineWidth = 8;
            if (inBasicEdgeMode || inMagicPathEdgeMode) {
                if (isEdgeStart) {
                    ctx.strokeStyle = "#FA5750";
                    ctx.fillStyle = "#FA5750";
                } else {
                    ctx.strokeStyle = "#FA5750";
                    ctx.fillStyle = "white";
                }
            } else {
                ctx.strokeStyle = "#32BFE3";
                ctx.fillStyle = "#32BFE3";
            }

            let oscillator = Math.cos(nodes[i].data.counter / 2 + 8); // oscillates -1.0 to 1.0
            let dampener = Math.min(1, 1 / (nodes[i].data.counter / 2)) + 0.05;
            let dampener2 = Math.min(1, 1 / (nodes[i].data.counter / 10));
            let radius = Math.max(
                1,
                25 * oscillator * dampener * dampener2 + nodeRadius
            );
            ctx.arc(nodes[i].data.x, nodes[i].data.y, radius, 0, Math.PI * 2, false);
            ctx.stroke();
            ctx.fill();

            // hover effects
            if (nodes[i].key === nodeHover?.key && !basicTool.state.stillInNode) {
                ctx.closePath();
                ctx.beginPath();
                if (!basicTool.state.edgeMode) {
                    ctx.lineWidth = 4;
                    ctx.arc(nodes[i].data.x, nodes[i].data.y, radius + 10, 0, Math.PI * 2, false);
                    ctx.stroke();
                } else {
                    ctx.fillStyle = "#FA5750";
                    ctx.arc(nodes[i].data.x, nodes[i].data.y, radius - 4, 0, Math.PI * 2, false);
                    ctx.fill();
                }
            }
            // increment "time" counter on nodes for bouncy animation; to prevent overflow, don't increment indefinitely
            if (nodes[i].counter < 1000) {
                nodes[i].counter += 1;
                const nodeData = {
                    counter: nodes[i].data.counter,
                    x: nodes[i].data.x,
                    y: nodes[i].data.y
                };
                graphController.updateNodeData(nodes[i].key, nodeData);
            }

            // labels on nodes
            if (labelsVisible) {
                ctx.font = "1rem Arial";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                const hasWhiteBackground = (inBasicEdgeMode || inMagicPathEdgeMode) && !isEdgeStart && nodes[i].key != nodeHover?.key;
                ctx.fillStyle = hasWhiteBackground ? "#FA5750" : "white";
                let label = nodes[i].key;
                const ADJUSTMENT = 1.5; // Ugh, textBaseline above doesn't help center on node properly so this makes it more centered
                ctx.fillText(label, nodes[i].data.x, nodes[i].data.y + ADJUSTMENT);
            }
        }

        if (
            toolState.curTool === areaCompleteTool &&
            areaCompleteTool.state.mousePressed
        ) {
            ctx.lineWidth = 1.5;
            ctx.strokeStyle = "red";
            ctx.fillStyle = "rgba(255, 130, 172, 0.15)";
            ctx.setLineDash([5, 5]);
            ctx.beginPath();
            let drawPoints = areaCompleteTool.state.drawPoints;
            let cur = drawPoints[0];
            for (let j = 1; j < drawPoints.length; j++) {
                cur = drawPoints[j];
                ctx.lineTo(cur.x, cur.y);
            }

            ctx.stroke();
            ctx.fill();
        }

        // dotted circle target for magic path tool
        if (toolState.curTool === magicPathTool && magicPathTool.state.edgeMode) {
            ctx.closePath();
            ctx.beginPath();
            ctx.lineWidth = 2;
            ctx.setLineDash([5, 5]);
            ctx.strokeStyle = "black";
            ctx.arc(mouseX, mouseY, 30, 0, Math.PI * 2, false);
            ctx.closePath();
            ctx.stroke();
        }
    }
    window.requestAnimationFrame(draw);
}

// What happens when the mouse is clicked (on the canvas)
function mouseDown(event) {
    let canvasBounds = canvas.getBoundingClientRect();
    // floor values to keep everything as integers, better for canvas rendering and generally better to keep things as ints
    let x = Math.floor(event.x - canvasBounds.left);
    let y = Math.floor(event.y - canvasBounds.top);

    if (toolState.curTool === areaCompleteTool) {
        areaCompleteTool.state.mousePressed = true;
        return;
    }

    let nodeClicked = nodeAtPoint(x, y, graphController.getFullNodeData());

    if (toolState.curTool === magicPathTool) {
        if (nodeClicked && !magicPathTool.state.edgeMode) {
            enterMagicEdgeMode(nodeClicked);
        } else if (!nodeClicked && magicPathTool.state.edgeMode) {
            exitMagicEdgeMode();
        }
        return;
    }

    if (toolState.curTool == basicTool) {
        if (!basicTool.state.edgeMode && !nodeClicked) {
            // create new Node
            graphController.pushUndoState();
            const nextKey = graphController.nextNodeKey();
            let newNode = new NodeData(nextKey, 0, x, y);
            graph.addNode(newNode);
            graphController.addNode(nextKey, {
                counter: 0,
                x: x,
                y: y
            });
            basicTool.state.stillInNode = true;
            refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
        } else if (!basicTool.state.edgeMode) {
            enterBasicEdgeMode(nodeClicked);
        } else if (nodeClicked && nodeClicked?.key != basicTool.state.edgeStart) {
            // add edge
            if (!graphController.containsEdge(basicTool.state.edgeStart, nodeClicked?.key)) {
                graphController.pushUndoState();
                const startNode = basicTool.state.edgeStart;
                graphController.addEdge(startNode, nodeClicked.key);
            }
            basicTool.state.edgeStart = nodeClicked?.key;
            refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
        } else {
            // leave edge mode
            exitBasicEdgeMode();
        }
    }

    if (toolState.curTool == moveTool) {
        // TODO: only save undo state if node actually is moved. Requires saving a 'pending' state before pushing it to the undo stack
        graphController.pushUndoState();
        moveTool.state.node = nodeClicked?.key;
    }
}

function clearGraph() {
    graphController.pushUndoState();
    graphController.clearGraph();
    graph = new Graph();
    exitBasicEdgeMode();
    exitMagicEdgeMode();
    toolState.curTool = basicTool;
    nodeHover = null;
    basicTool.state.stillInNode = false;
    refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
}

function mouseLeave(event) {
    exitBasicEdgeMode();
    exitMagicEdgeMode();
}

function mouseMove(event) {
    let canvasBounds = canvas.getBoundingClientRect();
    mouseX = event.x - canvasBounds.left;
    mouseY = event.y - canvasBounds.top;

    nodeHover = nodeAtPoint(mouseX, mouseY, graphController.getFullNodeData());
    if (!nodeHover) {
        basicTool.state.stillInNode = false;
    }

    if (
        toolState.curTool === areaCompleteTool &&
        areaCompleteTool.state.mousePressed
    ) {
        areaCompleteTool.state.drawPoints.push(new Point(mouseX, mouseY));
    }

    if (
        nodeHover &&
        toolState.curTool === magicPathTool &&
        magicPathTool.state.edgeMode &&
        nodeHover.key !== magicPathTool.state.edgeStart
    ) {
        if (!graph.containsEdge(magicPathTool.state.edgeStart, nodeHover.key)) {
            graphController.pushUndoState();
            const startNode = magicPathTool.state.edgeStart;
            graph.addEdge(startNode, nodeHover.key);
            graphController.addEdge(startNode, nodeHover.key);
        }
        magicPathTool.state.edgeStart = nodeHover.key;
        refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
    }

    if (toolState.curTool == moveTool) {
        if (moveTool.state.node != null) {
            // preserve counter value while updating position
            const counter = graphController.getNodeData(moveTool.state.node).counter;
            const updatedNodeData = {
                counter: counter,
                x: Math.floor(mouseX),
                y: Math.floor(mouseY)
            };
            graphController.updateNodeData(moveTool.state.node, updatedNodeData);
        }
    }
}

// What happens when the mouse is released (on the canvas)
function mouseUp() {
    if (toolState.curTool == moveTool) {
        if (moveTool.state.node != null) {
            moveTool.state.node = null;
        }
    }

    if (toolState.curTool == areaCompleteTool) {
        let selectionArea = areaCompleteTool.state.drawPoints.map((pt) => [
            pt.x,
            pt.y,
        ]);

        let selected = Array.from(graph.getNodeValues()).filter((n) => {
            let pt = [n.x, n.y];
            return inside(pt, selectionArea);
        });

        if (selected.length > 0) {
            let anyEdgesAdded = false;
            let graphClone = graph.clone(cloneNodeData);
            for (let i = 0; i < selected.length; i++) {
                for (let j = 0; j < selected.length; j++) {
                    if (i != j) {
                        // don't allow self edges
                        let edgeAdded = graph.addEdge(selected[i], selected[j]);
                        graphController.addEdge(selected[i].key, selected[j].key);
                        anyEdgesAdded = anyEdgesAdded || edgeAdded;
                    }
                }
            }
            if (anyEdgesAdded) {
                graphController.pushUndoState();
            }
        }
    }

    areaCompleteTool.state.mousePressed = false;
    areaCompleteTool.state.drawPoints = [];
    refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
}

// =====================
// Undo/Redo
// =====================
function undo() {
    graphController.popUndoState();
    refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
}

// =====================
// UI Refresh/Utility Functions
// =====================
function printDimensions(headerMessage) {
    // For debugging canvas size issues, not currently used but will probably use again in the future
    if (headerMessage) {
        console.log(headerMessage);
    }
    console.log(
        "window.innerWidth - infoPaneWidth: " + (window.innerWidth - infoPaneWidth) + "\n" +
        "window.innerHeight: " + window.innerHeight + "\n" +
        "canvas.style.width: " + canvas.style.width + "\n" +
        "canvas.style.height: " + canvas.style.height
    );
    if (canvas.getContext) {
        let ctx = canvas.getContext("2d");
        console.log(
            "canvas.width: " + ctx.canvas.width + "\n" +
            "canvas.height: " + ctx.canvas.height + "\n"
        );
    }
}

/** nodes is array of KeyWithData */
function nodeAtPoint(x, y, nodes) {
    for (const node of nodes) {
        let dx = x - node.data.x;
        let dy = y - node.data.y;
        let distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        if (distFromCent < nodeRadius * 2) {
            return node;
        }
    }
    return null;
}

function refreshHtml(nodeCount, edgeCount, toolState, graphTypes, adjList, adjacencyMatrix, matrixHoverCell) {
    // TODO? stop passing in args to refreshHtml and instead just call graphController from within here
    // We have to get the state at some point and I don't think there's any point in getting it in 10 diff place?
    // TODO: maybe only calculate if graph has changed (but don't worry about it until if/when performance becomes an issue)

    refreshToolbarHtml(toolState);
    refreshGraphInfoHtml(nodeCount, edgeCount, graphTypes);
    refreshAdjListHtml(adjList);
    refreshAdjMatrixHtml(adjList, adjacencyMatrix, matrixHoverCell);
    refreshDirectedButtonIcon();
}

function refreshGraphInfoHtml(nodeCount, edgeCount, graphTypes) {
    document.getElementById("node-count").innerHTML = nodeCount;
    document.getElementById("edge-count").innerHTML = edgeCount;
    document.getElementById("graph-types").innerHTML = graphTypes;
}

// adjListLabels is a 2d array of strings or numbers or whatever the label is for each node (?)
function refreshAdjListHtml(adjListLabels) {
    let adjListElem = document.getElementById("adjacency-list");
    if (adjListElem) {
        adjListElem.innerHTML = "";
        for (let i = 0; i < adjListLabels.length; i++) {
            var node = document.createElement("LI");
            var textnode = document.createTextNode(i + ":");
            node.appendChild(textnode);
            for (let j = 0; j < adjListLabels[i].length; j++) {
                node.appendChild(document.createTextNode(" " + adjListLabels[i][j]));
            }
            adjListElem.appendChild(node);
        }
    }
}

function refreshToolbarHtml(toolState) {
    for (const tool of toolState.allTools) {
        let toolElem = document.getElementById(tool.id);
        if (toolElem) toolElem.className = "tool-btn";
    }
    let curToolElem = document.getElementById(toolState.curTool.id);
    if (curToolElem) curToolElem.className = "tool-btn selected";
    canvasArea.style.cursor = toolState.curTool.cursor;

    let undoElem = document.getElementById("undo");
    if (undoElem) {
        undoElem.style.backgroundImage =
            graphController.canUndo() ?
            'url("images/undo-icon.svg")' :
            'url("images/undo-icon-gray.svg")';
    }
}

function setupClearButtonEventListener() {
    const clearButton = document.getElementById('clear-btn');
    clearButton.addEventListener('click', () => {
        clearGraph();
    });
}

setupClearButtonEventListener();

function inside(point, vs) {
    // ray-casting algorithm based on
    // https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html/pnpoly.html

    let x = point[0];
    let y = point[1];

    let inside = false;
    for (let i = 0, j = vs.length - 1; i < vs.length; j = i++) {
        let xi = vs[i][0],
            yi = vs[i][1];
        let xj = vs[j][0],
            yj = vs[j][1];
        let intersect =
            yi > y != yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi;
        if (intersect) inside = !inside;
    }
    return inside;
}

function refreshAdjMatrixHtml(adjList, adjacencyMatrix, matrixHoverCell) {
    let adjMatrixElem = document.getElementById("adj-matrix");
    if (adjMatrixElem && adjMatrixElem.getContext) {
        let totalWidth = adjMatrixElem.offsetWidth;
        let totalHeight = adjMatrixElem.offsetHeight;
        let ctx = adjMatrixElem.getContext("2d");
        ctx.clearRect(0, 0, totalWidth, totalHeight);

        let width = totalWidth / adjList.length;
        let height = totalHeight / adjList.length;
        let adjMatrix = adjacencyMatrix;

        // colors
        const edgePresentColor = "black";
        const hoverEdgePresentColor = "#F2813B"; // "#45ABD3"; // orange, or darker blue
        const hoverNoEdgeColor = "#E2E2E2";

        // fill in grid cells for each connected edge
        ctx.fillStyle = edgePresentColor;
        for (let i = 0; i < adjMatrix.length; i++) {
            for (let j = 0; j < adjMatrix[i].length; j++) {
                if (adjMatrix[i][j]) {
                    ctx.fillRect(width * i, height * j, width, height);
                }
            }
        }

        const isHovering = matrixHoverCell.length > 0

        // Color hovered cell (draws over previous coloring)
        // Check we have >1 node, since adding hover over 1 node is confusing since we can't do anything with it anyways (currently self-loops not allowed)
        if (isHovering && adjacencyMatrix.length > 1) {
            if (adjMatrix[matrixHoverCell[0]][matrixHoverCell[1]]) {
                ctx.fillStyle = hoverEdgePresentColor;
            } else {
                ctx.fillStyle = hoverNoEdgeColor;
            }
            ctx.fillRect(width * matrixHoverCell[0], height * matrixHoverCell[1], width, height);
        }
        ctx.fillStyle = edgePresentColor; // reset color

        // draw grid lines (but only when hovering)
        if (isHovering) {
            ctx.beginPath();
            for (let i = 1; i < adjMatrix.length; i++) {
                ctx.lineWidth = 1;
                ctx.strokeStyle = "lightgray";
                // vertical lines
                ctx.moveTo(width * i, 0);
                ctx.lineTo(width * i, totalWidth);
                // horizontal lines
                ctx.moveTo(0, height * i);
                ctx.lineTo(totalHeight, height * i);
            }
            ctx.closePath();
            ctx.stroke();
        }
    }
}

if (adjMatrixElem) {
    adjMatrixElem.addEventListener("mousemove", function(event) {
        const rect = adjMatrixElem.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        const nodeCount = graphController.nodeCount();
        if (nodeCount > 0) {
            const cellWidth = adjMatrixElem.width / (nodeCount * scale);
            const cellHeight = adjMatrixElem.height / (nodeCount * scale);
            const col = Math.floor(x / cellWidth);
            const row = Math.floor(y / cellHeight);
            graphController.hoverAdjMatrixCell(col, row);
        }
        refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
    });

    adjMatrixElem.addEventListener("mouseleave", function(event) {
        graphController.leaveAdjMatrix();
        // need to refresh html or hover color will linger
        refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
    });

    adjMatrixElem.addEventListener("mouseup", function(event) {
            graphController.adjMatrixClick();
        });
}

function enterBasicEdgeMode(node) {
    basicTool.state.edgeMode = true;
    basicTool.state.edgeStart = node.key;
}

function exitBasicEdgeMode() {
    basicTool.state.edgeMode = false;
    basicTool.state.edgeStart = null;
}

function exitMagicEdgeMode() {
    magicPathTool.state.edgeMode = false;
    magicPathTool.state.edgeStart = null;
    magicPathTool.cursor = magicPathTool.state.normalCursor;
    refreshToolbarHtml(toolState);
}

function enterMagicEdgeMode(node) {
    magicPathTool.state.edgeMode = true;
    magicPathTool.state.edgeStart = node.key;
    magicPathTool.cursor = magicPathTool.state.noneCursor;
}

// Info/Export Pane Event Handlers
document.getElementById("export-pane-select").addEventListener(
    "click",
    () => {
        for (const elem of document.getElementsByClassName("info-pane-only")) {
            elem.style.display = "none";
        }
        for (const elem of document.getElementsByClassName("export-pane-only")) {
            elem.style.display = "block";
        }
        document.getElementById("export-pane-select").style.color = "black";
        document.getElementById("export-pane-select").style.fontWeight = "bold";
        document.getElementById("info-pane-select").style.color = "gray";
        document.getElementById("info-pane-select").style.fontWeight = "normal";
    },
    false
);

document.getElementById("info-pane-select").addEventListener(
    "click",
    () => {
        for (const elem of document.getElementsByClassName("export-pane-only")) {
            elem.style.display = "none";
        }
        for (const elem of document.getElementsByClassName("info-pane-only")) {
            elem.style.display = "block";
        }
        document.getElementById("info-pane-select").style.color = "black";
        document.getElementById("info-pane-select").style.fontWeight = "bold";
        document.getElementById("export-pane-select").style.color = "gray";
        document.getElementById("export-pane-select").style.fontWeight = "normal";
    },
    false
);

for (const copyBtn of document.getElementsByClassName("copy-btn")) {
    copyBtn.addEventListener(
        "click",
        () => {
            navigator.clipboard.writeText(graphController.getDot).then(
                function() {},
                function() {
                    console.assert(false, "Clipboard write failed.");
                }
            );
        },
        false
    );
}

let labelVisibleBtn = document.getElementById("label-visible-btn");
labelVisibleBtn.addEventListener(
    "click",
    () => {
        if (document.getElementById("visible-icon")) {
            document.getElementById("visible-icon").src = labelsVisible ?
                "images/invisible-icon.svg" :
                "images/node-label-visible.svg";
            labelsVisible = !labelsVisible;
        }
    },
    false
);

function refreshDirectedButtonIcon() {
    if (document.getElementById("directed-icon")) {
        document.getElementById("directed-icon").src = graphController.isDirected() ?
            "images/arrow-small-1-blue.svg" :
            "images/arrow-small-1.svg";
    }
    if (document.getElementById("directed-btn")) {
        document.getElementById("directed-btn").style.backgroundColor = graphController.isDirected() ?
            "#cff5ff" :
            "white";
    }
}

let directedBtn = document.getElementById("directed-btn");
directedBtn.addEventListener(
    "click",
    () => {
        graphController.pushUndoState();
        graphController.toggleDirectionality();
        refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());
    },
    false
);

directedBtn.addEventListener(
    "mouseenter",
    () => {
        if (document.getElementById("directed-btn")) {
            document.getElementById("directed-btn").style.backgroundColor = graphController.isDirected() ?
                "#cce8f0" :
                "lightgray";
        }
    },
    false
);

directedBtn.addEventListener(
    "mouseleave",
    () => {
        if (document.getElementById("directed-btn")) {
            document.getElementById("directed-btn").style.backgroundColor = graphController.isDirected() ?
                "#ebfaff" :
                "white";
        }
    },
    false
);

// Listener for cmd+Z undo. This listens for key presses on the entire document.
document.addEventListener('keydown', function(event) {
    // Check if the 'z' key was pressed (case-insensitive)
    if (event.key.toLowerCase() === 'z') {

        // Check if the Command key (on Mac) or Control key (on Windows/Linux)
        // is being held down at the same time.
        const isUndo = event.metaKey || event.ctrlKey;

        if (isUndo) {
            // This is the "undo" command.

            // Prevent the browser's default undo action (e.g., in a text field)
            event.preventDefault();

            // Call your custom function
            undo();
        }
    }
});

refreshHtml(graphController.nodeCount(), graphController.edgeCount(), toolState, calculateGraphType(graph), graphController.getAdjList(), graphController.getAdjacencyMatrix(), graphController.getMatrixHoverCell());