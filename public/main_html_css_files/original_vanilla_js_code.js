import {
    Graph,
    Digraph
} from "../algorithms/graph.mjs";
import {
    calculateGraphType
} from "../algorithms/graph_algs.mjs";

// =====================
// Class/Type Definitions
// =====================

function Point(x, y) {
    this.x = x;
    this.y = y;
}

// =====================
// State and Constants
// =====================
const nodeRadius = 18;
let canvas = document.getElementById("main-canvas-upper");
let canvasArea = document.getElementById("canvas-area");
const infoPaneWidth = document.getElementsByClassName("right-pane")?.[0].offsetWidth;
let graph = new Digraph();
const graphController = Main.getGraphController();
let mouseX = 0;
let mouseY = 0;
let infoPaneHover = false;
let labelsVisible = true;
const timeInit = new Date().getSeconds();
let printCounter = 0;
let scale = window.devicePixelRatio;
let graphTypes = [];

// =====================
// Canvas Setup
// =====================
// TODO: Things don't work if I don't do this, but I suspect if I set up the css properly, this shouldn't be necessary?
// (except for the scale issue, I'm not sure that can be solved with css alone)
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

// =====================
// Event Listeners
// =====================
window.addEventListener('resize', function(event) {
    setCanvasSize()
});

document.onkeydown = function(event) {
    event = event || window.event;
    var isEscape = false;
    if ("key" in event) {
        isEscape = event.key === "Escape" || event.key === "Esc";
    }
};

let undoElem = document.getElementById("undo");
if (undoElem) {
    undoElem.addEventListener("click", undo, false);
}

// =====================
// Undo/Redo
// =====================
function undo() {
    graphController.popUndoState();
    refreshHtml(graphController.nodeCount(), graphController.edgeCount(), calculateGraphType(graph), graphController.getAdjList());
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

function refreshHtml(nodeCount, edgeCount, graphTypes, adjList) {
    // TODO? stop passing in args to refreshHtml and instead just call graphController from within here
    // We have to get the state at some point and I don't think there's any point in getting it in 10 diff place?
    // TODO: maybe only calculate if graph has changed (but don't worry about it until if/when performance becomes an issue)

    refreshGraphInfoHtml(nodeCount, edgeCount, graphTypes);
    refreshAdjListHtml(adjList);
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
        refreshHtml(graphController.nodeCount(), graphController.edgeCount(), calculateGraphType(graph), graphController.getAdjList());
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

refreshHtml(graphController.nodeCount(), graphController.edgeCount(), calculateGraphType(graph), graphController.getAdjList());