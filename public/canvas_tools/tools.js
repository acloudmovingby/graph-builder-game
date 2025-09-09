import { Graph, Digraph } from "../algorithms/graph.mjs";
import { calculateGraphType, getDot } from "../algorithms/graph_algs.mjs";
import { drawDirectedEdges, drawSimpleEdges } from "./render/edge_render.mjs";
import { nodeRadius } from "./render/node_render.mjs";

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
let canvas = document.getElementById("canvas");
let canvasArea = document.getElementById("canvas-area");
const infoPaneWidth = document.getElementsByClassName("info-panel")?.[0].offsetWidth;
let graph = new Digraph();
const graphController = new GraphController();
var graphKeyCounter = 0; // used to give each node a unique key when created
let mouseX = 0;
let mouseY = 0;
let nodeHover = null;
let infoPaneHover = false;
let labelsVisible = true;
const timeInit = new Date().getSeconds();
let printCounter = 0;
let canvasWidth = window.innerWidth - infoPaneWidth;
let canvasHeight = window.innerHeight;
let scale = window.devicePixelRatio;
let undoGraphStates = [];
let graphTypes = [];

// =====================
// Assertions About State
// ====================
function runAssertions() {
    console.assert(graphController.nodeCount() == graph.nodeCount, "GraphController and Graph node counts don't match (" + graphController.nodeCount() + " vs " + graph.nodeCount + ")");
    console.assert(graphController.edgeCount() == graph.edgeCount, "GraphController and Graph edge counts don't match (" + graphController.edgeCount() + " vs " + graph.edgeCount + ")");
}

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
  ),
  {
    edgeMode: false,
    edgeStart: null,
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
  ),
  {
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
  ),
  {
    edgeMode: false,
    edgeStart: null,
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
  ),
  {
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
function setCanvasSize() {
  canvas.style.width = canvasWidth + "px";
  canvas.style.height = canvasHeight + "px";
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
for (const tool of toolState.allTools) {
  if (document.getElementById(tool.id)) {
    document.getElementById(tool.id).addEventListener(
      "click",
      () => {
        toolState.curTool = tool;
        refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
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

document.onkeydown = function (event) {
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
  canvas.addEventListener("mousedown", canvasClick, false);
  canvas.addEventListener("mousemove", mouseMove, false);
  canvas.addEventListener("mouseleave", mouseLeave, false);
  canvas.addEventListener("mouseup", mouseUp, false);
  window.requestAnimationFrame(draw);
}

// =====================
// Main Drawing and Core Logic
// =====================
function draw() {
  if (canvas.getContext) {
    let ctx = canvas.getContext("2d");

    setCanvasSize()

    ctx.clearRect(0, 0, window.innerWidth * 2, window.innerHeight * 2);

    const welcome = document.getElementById('welcome-message');
    welcome.style.visibility = graph.nodeCount === 0 ? "visible" : "hidden";

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
      ctx.moveTo(edgeStart.x, edgeStart.y);
      ctx.lineTo(mouseX, mouseY);
      ctx.closePath();
      ctx.stroke();
    }

    // draw edges
    const edges = graph.getEdges();
    // TODO have an if here when it comes time to toggle back and forth between directed and undirected
    //drawSimpleEdges(ctx, edges.map(e => [e[0].x, e[0].y, e[1].x, e[1].y]));
    drawDirectedEdges(ctx, edges.map(e => [e[0].x, e[0].y, e[1].x, e[1].y]));

    // draw nodes
    let nodes = Array.from(graph.getNodeValues());
    for (let i = 0; i < nodes.length; i++) {
      const isEdgeStart = nodes[i] === toolState.curTool.state.edgeStart;
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

      let oscillator = Math.cos(nodes[i].counter / 2 + 8); // oscillates -1.0 to 1.0
      let dampener = Math.min(1, 1 / (nodes[i].counter / 2)) + 0.05;
      let dampener2 = Math.min(1, 1 / (nodes[i].counter / 10));
      let radius = Math.max(
        1,
        25 * oscillator * dampener * dampener2 + nodeRadius
      );
      ctx.arc(nodes[i].x, nodes[i].y, radius, 0, Math.PI * 2, false);
      ctx.stroke();
      ctx.fill();

      // hover effects
      if (nodes[i] === nodeHover && !basicTool.state.stillInNode) {
        ctx.closePath();
        ctx.beginPath();
        if (!basicTool.state.edgeMode) {
          ctx.lineWidth = 4;
          ctx.arc(nodes[i].x, nodes[i].y, radius + 10, 0, Math.PI * 2, false);
          ctx.stroke();
        } else {
          ctx.fillStyle = "#FA5750";
          ctx.arc(nodes[i].x, nodes[i].y, radius - 4, 0, Math.PI * 2, false);
          ctx.fill();
        }
      }
      // increment "time" counter on nodes for bouncy animation; to prevent overflow, don't increment indefinitely
      if (nodes[i].counter < 1000) {
        nodes[i].counter += 1;
        const nodeData = { counter: nodes[i].counter, x: nodes[i].x, y: nodes[i].y };
        graphController.updateNodeData(nodes[i].key, nodeData);
      }

      // labels on nodes
      if (labelsVisible) {
        ctx.font = "1rem Arial";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        const hasWhiteBackground = (inBasicEdgeMode || inMagicPathEdgeMode) && !isEdgeStart && nodes[i] != nodeHover;
        ctx.fillStyle = hasWhiteBackground ? "#FA5750" : "white";
        let label = graph.nodeValues.get(nodes[i]);
        const ADJUSTMENT = 2; // textBaseline above doesn't help center on node properly so this makes it more centered
        ctx.fillText(label, nodes[i].x, nodes[i].y + ADJUSTMENT);
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
  runAssertions();
  window.requestAnimationFrame(draw);
}

function canvasClick(event) {
  let canvasBounds = canvas.getBoundingClientRect();
  // floor values to keep everything as integers, better for canvas rendering and generally better to keep things as ints
  let x = Math.floor(event.x - canvasBounds.left);
  let y = Math.floor(event.y - canvasBounds.top);

  if (toolState.curTool === areaCompleteTool) {
    areaCompleteTool.state.mousePressed = true;
    return;
  }

  let nodeClicked = nodeAtPoint(x, y, graph.getNodeValues());

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
      addToUndo(undoGraphStates, graph);
      graphController.pushUndoState();
      let newNode = new NodeData(graphKeyCounter, 0, x, y);
      graph.addNode(newNode);
      graphController.addNode(graphKeyCounter, {
            counter: 0,
            x: x,
            y: y
      });
      graphKeyCounter += 1;
      basicTool.state.stillInNode = true;
      refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
    } else if (!basicTool.state.edgeMode) {
      enterBasicEdgeMode(nodeClicked);
    } else if (nodeClicked && nodeClicked != basicTool.state.edgeStart) {
      // add edge
      if (!graph.containsEdge(basicTool.state.edgeStart, nodeClicked)) {
        addToUndo(undoGraphStates, graph);
        graphController.pushUndoState();
        const startNode = basicTool.state.edgeStart;
        graph.addEdge(startNode, nodeClicked);
        graphController.addEdge(startNode.key, nodeClicked.key);
      }
      basicTool.state.edgeStart = nodeClicked;
      refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
    } else {
      // leave edge mode
      exitBasicEdgeMode();
    }
  }

  if (toolState.curTool == moveTool) {
    addToUndo(undoGraphStates, graph);
    graphController.pushUndoState();
    moveTool.state.node = nodeClicked;
  }
  runAssertions();
}

function clearGraph() {
  addToUndo(undoGraphStates, graph);
  graphController.pushUndoState();
  graph = new Graph();
  exitBasicEdgeMode();
  exitMagicEdgeMode();
  toolState.curTool = basicTool;
  nodeHover = null;
  basicTool.state.stillInNode = false;
  refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
}

function mouseLeave(event) {
  exitBasicEdgeMode();
  exitMagicEdgeMode();
}

function mouseMove(event) {
  let canvasBounds = canvas.getBoundingClientRect();
  mouseX = event.x - canvasBounds.left;
  mouseY = event.y - canvasBounds.top;

  nodeHover = nodeAtPoint(mouseX, mouseY, graph.getNodeValues());
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
    nodeHover !== magicPathTool.state.edgeStart
  ) {
    if (!graph.containsEdge(magicPathTool.state.edgeStart, nodeHover)) {
      addToUndo(undoGraphStates, graph);
      graphController.pushUndoState();
      const startNode = magicPathTool.state.edgeStart;
      graph.addEdge(startNode, nodeHover);
      graphController.addEdge(startNode.key, nodeHover.key);
    }
    magicPathTool.state.edgeStart = nodeHover;
    refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
  }

  if (toolState.curTool == moveTool) {
    if (moveTool.state.node) {
      moveTool.state.node.x = mouseX;
      moveTool.state.node.y = mouseY;
    }
  }
}

function mouseUp() {
  if (toolState.curTool == moveTool && moveTool.state.node) {
    moveTool.state.node = null;
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
        addToUndo(undoGraphStates, graphClone);
        graphController.pushUndoState();
      }
    }
  }

  areaCompleteTool.state.mousePressed = false;
  areaCompleteTool.state.drawPoints = [];
  refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
}

// =====================
// Undo/Redo
// =====================
function undo() {
  if (undoGraphStates.length > 0) {
    graph = undoGraphStates.pop();
    graphController.popUndoState();
    refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());
  }
}

function addToUndo(undoGraphStates, graph) {
  const UNDO_SIZE_LIMIT = 25;
  undoGraphStates.push(graph.clone(cloneNodeData));
  if (undoGraphStates.length > UNDO_SIZE_LIMIT) {
    undoGraphStates.shift(1);
  }
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

function nodeAtPoint(x, y, nodes) {
  for (const node of nodes) {
    let dx = x - node.x;
    let dy = y - node.y;
    let distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    if (distFromCent < nodeRadius * 2) {
      return node;
    }
  }
  return null;
}

function refreshHtml(nodeCount, edgeCount, toolState, graphTypes, adjList) {
  // TODO: maybe only calculate if graph has changed (but don't worry about it until if/when performance becomes an issue)

  refreshToolbarHtml(toolState);
  refreshGraphInfoHtml(nodeCount, edgeCount, graphTypes);
  refreshAdjListHtml(adjList);
  refreshAdjMatrixHtml(nodeCount, adjList);
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
      undoGraphStates.length === 0
        ? 'url("images/undo-icon-gray.svg")'
        : 'url("images/undo-icon.svg")';
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

function refreshAdjMatrixHtml(nodeCount, adjList) {
  let matrixElem = document.getElementById("adj-matrix");
  if (matrixElem && matrixElem.getContext) {
    let totalWidth = matrixElem.offsetWidth;
    let totalHeight = matrixElem.offsetHeight;
    let ctx = matrixElem.getContext("2d");
    ctx.clearRect(0, 0, totalWidth, totalHeight);

    let width = totalWidth / nodeCount;
    let height = totalHeight / nodeCount;
    let adjMatrix = generateAdjacencyMatrix(adjList);
    for (let i = 0; i < adjMatrix.length; i++) {
      for (let j = 0; j < adjMatrix[i].length; j++) {
        if (adjMatrix[i][j]) {
          ctx.fillRect(width * i, height * j, width, height);
        }
      }
    }
  }
}

// boolean 2d array; length of array is number of nodes; true means an edge exists between those nodes
function generateAdjacencyMatrix(adjList) {
  // initialize 2d array with false
  let adjMatrix = Array.from({ length: graph.nodeCount }).map((n) =>
    Array.from({ length: graph.nodeCount }).map((x) => (x = false))
  );
  for (let i = 0; i < adjList.length; i++) {
    for (let j = 0; j < adjList[i].length; j++) {
      let targetIndex = adjList[i][j];
      adjMatrix[i][targetIndex] = true;
    }
  }
  return adjMatrix;
}

function enterBasicEdgeMode(node) {
  basicTool.state.edgeMode = true;
  basicTool.state.edgeStart = node;
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
  magicPathTool.state.edgeStart = node;
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
      navigator.clipboard.writeText(getDot(graph)).then(
        function () {},
        function () {
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
      document.getElementById("visible-icon").src = labelsVisible
        ? "images/invisible-icon.svg"
        : "images/node-label-visible.svg";
      labelsVisible = !labelsVisible;
    }
  },
  false
);

refreshHtml(graph.nodeCount, graph.edgeCount, toolState, calculateGraphType(graph), graph.getAdjList());

