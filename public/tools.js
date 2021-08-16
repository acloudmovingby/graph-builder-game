let canvas = document.getElementById("canvas");
const infoPaneWidth = 300; // this MUST match the grid-template-columns max width in .container in the CSS file
let graph = new Graph();
let edgeMode = false;
let edgeStart = null;
let mouseX = 0;
let mouseY = 0;
let nodeHover = null;
let stillInNode = false; // true if mouse is still inside node bounds for a node that was just created, so you don't immediately get a hover effect after creating the node but only starts happening after you've left that node's location
let clearButtonHover = false;

const timeInit = new Date().getSeconds();
const nodeRadius = 15;

const toolModes = {
  BASIC: "basic",
  AREACOMPLETE: "area-complete",
  RECTSELECT: "rect-select",
};
let tool = toolModes.BASIC;
canvas.style.cursor = "url('images/pointer.svg'), pointer";

let mousePressed = false; // for area complete tool
let drawPoints = []; // points for selection area of area complete tool

let rectSelectStart = new Point(0, 0);

let basicTool = document.getElementById("basic");
let areaCompleteTool = document.getElementById("area-complete");
let rectSelectTool = document.getElementById("rect-select");
if (basicTool && areaCompleteTool && rectSelectTool) {
  areaCompleteTool.addEventListener(
    "click",
    () => setToolMode(toolModes.AREACOMPLETE),
    false
  );
  basicTool.addEventListener(
    "click",
    () => setToolMode(toolModes.BASIC),
    false
  );
  rectSelectTool.addEventListener(
    "click",
    () => setToolMode(toolModes.RECTSELECT),
    false
  );
}

function setToolMode(toolMode) {
  if (toolMode === toolModes.BASIC) {
    basicTool.className = "tool-btn selected";
    areaCompleteTool.className = "tool-btn";
    rectSelectTool.className = "tool-btn";
    canvas.style.cursor = "url('images/pointer.svg'), pointer";
    tool = toolModes.BASIC;
  } else if (toolMode === toolModes.AREACOMPLETE) {
    basicTool.className = "tool-btn";
    areaCompleteTool.className = "tool-btn selected";
    rectSelectTool.className = "tool-btn";
    canvas.style.cursor = "url('images/area-complete-cursor.svg'), pointer";
    tool = toolModes.AREACOMPLETE;
  } else {
    basicTool.className = "tool-btn";
    areaCompleteTool.className = "tool-btn";
    rectSelectTool.className = "tool-btn selected";
    canvas.style.cursor = "url('images/rect-select.svg'), pointer";
    tool = toolModes.RECTSELECT;
  }
}

if (canvas.getContext) {
  canvas.addEventListener("mousedown", canvasClick, false);
  canvas.addEventListener("mousemove", mouseMove, false);
  canvas.addEventListener("mouseleave", mouseLeave, false);
  canvas.addEventListener("mouseup", mouseUp, false);
  document.addEventListener("keydown", keyDown, false);
  document.addEventListener("keyup", keyUp, false);
  window.requestAnimationFrame(draw);
}

function draw() {
  if (canvas.getContext) {
    let ctx = canvas.getContext("2d");
    ctx.canvas.width = window.innerWidth - infoPaneWidth;
    ctx.canvas.height = window.innerHeight;
    ctx.clearRect(0, 0, window.innerWidth * 2, window.innerHeight * 2);

    // start message
    if (graph.nodeCount === 0) {
      ctx.font = "25px Arial";
      ctx.fillStyle = "gray";
      ctx.textAlign = "center";
      ctx.fillText(
        "Welcome! To start, try clicking somewhere",
        ctx.canvas.width / 2,
        ctx.canvas.height / 2
      );
    }

    // clear/reset message
    ctx.font = "1rem Arial";
    ctx.textAlign = "start";
    ctx.fillStyle = clearButtonHover ? "black" : "#909090";
    ctx.fillText("clear", 35, 35);

    //edge mode, draw edge from edgeStart to mouse cursor
    if (edgeMode) {
      ctx.beginPath();
      ctx.lineWidth = 8;
      ctx.strokeStyle = "#ffdc7a";
      ctx.moveTo(edgeStart.x, edgeStart.y);
      ctx.lineTo(mouseX, mouseY);
      ctx.closePath();
      ctx.stroke();
    }

    // draw edges
    let edges = graph.getEdges();
    edges.forEach((e) => {
      ctx.beginPath();
      ctx.lineWidth = 8;
      ctx.strokeStyle = "orange";
      ctx.moveTo(e[0].x, e[0].y);
      ctx.lineTo(e[1].x, e[1].y);
      ctx.closePath();
      ctx.stroke();
    });

    // draw nodes
    let nodes = Array.from(graph.getNodeValues());
    for (let i = 0; i < nodes.length; i++) {
      ctx.beginPath();
      ctx.lineWidth = 8;
      if (nodes[i] === edgeStart) {
        ctx.strokeStyle = "#FA5750";
        ctx.fillStyle = "#FA5750";
      } else if (edgeMode) {
        ctx.strokeStyle = "#FA5750";
        ctx.fillStyle = "white";
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
      if (nodes[i] === nodeHover && !stillInNode) {
        ctx.closePath();
        ctx.beginPath();
        if (!edgeMode) {
          ctx.lineWidth = 4;
          ctx.arc(nodes[i].x, nodes[i].y, radius + 10, 0, Math.PI * 2, false);
          ctx.stroke();
        } else {
          ctx.fillStyle = "#FA5750";
          ctx.arc(nodes[i].x, nodes[i].y, radius - 4, 0, Math.PI * 2, false);
          ctx.fill();
        }
      }
      // to prevent overflow, don't increment indefinitely
      if (nodes[i].counter < 1000) {
        nodes[i].counter += 1;
      }
    }

    if (mousePressed) {
      ctx.lineWidth = 1.5;
      ctx.strokeStyle = "red";
      ctx.fillStyle = "rgba(255, 130, 172, 0.15)";
      ctx.setLineDash([5, 5]);
      ctx.beginPath();
      if (tool === toolModes.AREACOMPLETE) {
        let cur = drawPoints[0];
        for (let j = 1; j < drawPoints.length; j++) {
          cur = drawPoints[j];
          ctx.lineTo(cur.x, cur.y);
        }
      }
      ctx.stroke();
      ctx.fill();
    }
  }
  window.requestAnimationFrame(draw);
}

// for the graph algorithms, I use only adjacency lists (as 2d arrays) for efficiency, but for drawing to the canvas, it's easier if I store state associated with that node all in one object.
function NodeData(counter, x, y) {
  this.counter = counter;
  this.x = x;
  this.y = y;
  this.neighbors = [];
}

// returns the node, if any, located at those coordinates. Assumes coordinates are relative to canvas, not window.
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

function canvasClick(event) {
  let canvasBounds = canvas.getBoundingClientRect();
  let x = event.x - canvasBounds.left;
  let y = event.y - canvasBounds.top;

  if (clearButtonHover) {
    clearGraph();
    return;
  }

  if (tool === toolModes.AREACOMPLETE || tool === toolModes.RECTSELECT) {
    mousePressed = true;
    //canvas.style.cursor = "url('images/area-complete-cursor-clicked.svg') 4 3, pointer";
    return;
  }

  let nodeClicked = nodeAtPoint(x, y, graph.getNodeValues());

  if (!edgeMode && !nodeClicked) {
    // create new Node
    let newNode = new NodeData(0, x, y);
    graph.addNode(newNode);
    stillInNode = true;
    setCommentary(graph);
    refreshGraphInfoHtml(graph);
    refreshAdjListHtml(graph);
    refreshAdjMatrixHtml(graph);
  } else if (!edgeMode) {
    // start edge on the node clicked
    edgeMode = true;
    edgeStart = nodeClicked;
  } else if (nodeClicked && nodeClicked != edgeStart) {
    // add edge
    graph.addEdge(edgeStart, nodeClicked);
    edgeStart = nodeClicked;
    setCommentary(graph);
    refreshGraphInfoHtml(graph);
    refreshAdjListHtml(graph);
    refreshAdjMatrixHtml(graph);
  } else {
    // cancel edge mode
    edgeMode = false;
    edgeStart = null;
  }
}

function clearGraph() {
  graph = new Graph();
  edgeMode = false;
  edgeStart = null;
  nodeHover = null;
  stillInNode = false;

  refreshGraphInfoHtml(graph);
  refreshAdjListHtml(graph);
  refreshAdjMatrixHtml(graph);
  setCommentary();
}

function mouseLeave(event) {
  edgeMode = false;
  edgeStart = null;
}

function mouseMove(event) {
  let canvasBounds = canvas.getBoundingClientRect();
  mouseX = event.x - canvasBounds.left;
  mouseY = event.y - canvasBounds.top;

  nodeHover = nodeAtPoint(mouseX, mouseY, graph.getNodeValues());
  if (!nodeHover) {
    stillInNode = false;
  }

  // hover over clear button
  if (mouseX > 0 && mouseX < 80 && mouseY > 0 && mouseY < 80) {
    clearButtonHover = true;
  } else {
    if (clearButtonHover) {
      clearButtonHover = false;
    }
  }

  if (mousePressed && tool === toolModes.AREACOMPLETE) {
    drawPoints.push(new Point(mouseX, mouseY));
  }
}

function mouseUp() {
  let selectionArea = drawPoints.map((pt) => [pt.x, pt.y]);

  let selected = Array.from(graph.getNodeValues()).filter((n) => {
    let pt = [n.x, n.y];
    return inside(pt, selectionArea);
  });
  for (let i = 0; i < selected.length; i++) {
    for (let j = 0; j < selected.length; j++) {
      if (i != j) {
        // don't allow self edges
        graph.addEdge(selected[i], selected[j]);
      }
    }
  }

  if (tool === toolModes.AREACOMPLETE) {
    canvas.style.cursor = "url('images/area-complete-cursor.svg'), pointer";
  }

  setCommentary();
  refreshGraphInfoHtml(graph);
  refreshAdjListHtml(graph);
  refreshAdjMatrixHtml(graph);
  mousePressed = false;
  drawPoints = [];
}

function keyDown(event) {
  if (event.code === "ShiftLeft") {
    setToolMode(toolModes.AREACOMPLETE);
  }
}

function keyUp(event) {
  if (event.code === "ShiftLeft") {
    setToolMode(toolModes.BASIC);
  }
}

function refreshGraphInfoHtml(graph) {
  document.getElementById("node-count").innerHTML = graph.nodeCount;
  document.getElementById("edge-count").innerHTML = graph.edgeCount;
}

function refreshAdjListHtml(graph) {
  let adjListElem = document.getElementById("adjacency-list");
  if (adjListElem) {
    let graphAdj = graph.getAdjList();
    adjListElem.innerHTML = "";
    for (let i = 0; i < graphAdj.length; i++) {
      var node = document.createElement("LI");
      var textnode = document.createTextNode(i + ":");
      node.appendChild(textnode);
      for (let j = 0; j < graphAdj[i].length; j++) {
        node.appendChild(document.createTextNode(" " + graphAdj[i][j]));
      }
      adjListElem.appendChild(node);
    }
  }
}

function setCommentary() {
  let commentary = "Nice graph!";
  document.getElementById("commentary").innerHTML =
    "&#34;" + commentary + "&#34;";
}

function Point(x, y) {
  this.x = x;
  this.y = y;
}

// Thank you!: https://stackoverflow.com/questions/22521982/check-if-point-is-inside-a-polygon
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

function refreshAdjMatrixHtml(graph) {
  let matrixElem = document.getElementById("adj-matrix");
  if (matrixElem && matrixElem.getContext) {
    let totalWidth = matrixElem.offsetWidth;
    let totalHeight = matrixElem.offsetHeight;
    let ctx = matrixElem.getContext("2d");
    ctx.clearRect(0, 0, totalWidth, totalHeight);

    let width = totalWidth / graph.nodeCount;
    let height = totalHeight / graph.nodeCount;
    let adjMatrix = generateAdjacencyMatrix(graph.getAdjList());
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
