let canvas = document.getElementById("canvas");
const infoPaneWidth = 300; // this MUST match the grid-template-columns max width in .container in the CSS file
let nodes = [];
let edgeMode = false;
let edgeStart = null;
let edgeCount = 0;
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
};
let tool = toolModes.BASIC;
canvas.style.cursor = "url('images/pointer.svg'), pointer";

let isDrawing = false; // for area complete tool
let drawPoints = []; // points for selection area of area complete tool

let basicTool = document.getElementById("basic");
let areaCompleteTool = document.getElementById("area-complete");
if (basicTool && areaCompleteTool) {
  areaCompleteTool.addEventListener("click", () => setToolMode(toolModes.AREACOMPLETE), false);
  basicTool.addEventListener("click", () => setToolMode(toolModes.BASIC), false);
}

function setToolMode(toolMode) {
  if (toolMode === toolModes.BASIC) {
    basicTool.className = "tool-btn selected";
    areaCompleteTool.className = "tool-btn";
    canvas.style.cursor = "url('images/pointer.svg'), pointer";
    tool = toolModes.BASIC;
  } else {
    basicTool.className = "tool-btn";
    areaCompleteTool.className = "tool-btn selected";
    canvas.style.cursor = "url('images/area-complete-cursor.svg'), pointer";
    tool = toolModes.AREACOMPLETE;
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
    if (nodes.length === 0) {
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
    let edges = getEdges(nodes);
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

    if (tool === toolModes.AREACOMPLETE && isDrawing) {
      ctx.lineWidth = 1.5;
      ctx.strokeStyle = "red";
      ctx.fillStyle = "rgba(255, 130, 172, 0.15)";
      ctx.setLineDash([5, 5]);
      ctx.beginPath();
      let cur = drawPoints[0];
      for (let j = 1; j < drawPoints.length; j++) {
        cur = drawPoints[j];
        ctx.lineTo(cur.x, cur.y);
      }
      ctx.stroke();
      ctx.fill();
    }
  }
  window.requestAnimationFrame(draw);
}

// for the graph algorithms, I use only adjacency lists (as 2d arrays) for efficiency, but for drawing to the canvas, it's easier if I store state associated with that node all in one object.
function Node(index, counter, x, y) {
  this.index = index;
  this.counter = counter;
  this.x = x;
  this.y = y;
  this.neighbors = [];
}

// returns the node, if any, located at those coordinates. Assumes coordinates are relative to canvas, not window.
function nodeAtPoint(x, y, nodes) {
  for (let i = 0; i < nodes.length; i++) {
    let dx = x - nodes[i].x;
    let dy = y - nodes[i].y;
    let distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    if (distFromCent < nodeRadius * 2) {
      return nodes[i];
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

  if (tool === toolModes.AREACOMPLETE) {
    isDrawing = true;
    //canvas.style.cursor = "url('images/area-complete-cursor-clicked.svg') 4 3, pointer";
    return;
  }

  let nodeClicked = nodeAtPoint(x, y, nodes);

  if (!edgeMode && !nodeClicked) {
    // create new Node
    nodes.push(new Node(nodes.length, 0, x, y));
    let node = document.createElement("LI");
    node.appendChild(document.createTextNode(nodes.length - 1 + ": "));
    document.getElementById("adjacency-list").appendChild(node);
    stillInNode = true;
    document.getElementById("node-count").innerHTML = nodes.length;
    setCommentary();
  } else if (!edgeMode) {
    // start edge on the node clicked
    edgeMode = true;
    edgeStart = nodeClicked;
  } else if (nodeClicked && nodeClicked != edgeStart) {
    if (!edgeStart.neighbors.includes(nodeClicked)) {
      edgeStart.neighbors.push(nodeClicked);
      nodeClicked.neighbors.push(edgeStart);

      edgeCount++;
      document.getElementById("edge-count").innerHTML = edgeCount;
      setCommentary();
      let adjList = document.getElementById("adjacency-list");
      if (adjList && adjList.hasChildNodes()) {
        let items = adjList.childNodes;
        let startIx = 0;
        let clickedIx = 0;
        for (let i = 0; i < nodes.length; i++) {
          if (nodes[i] === edgeStart) {
            startIx = i;
          }
          if (nodes[i] === nodeClicked) {
            clickedIx = i;
          }
        }
        items[startIx].appendChild(document.createTextNode(" " + clickedIx));
        items[clickedIx].appendChild(document.createTextNode(" " + startIx));
      }
      edgeStart = nodeClicked;
    }
    edgeStart = nodeClicked;
  } else {
    // cancel edge mode
    edgeMode = false;
    edgeStart = null;
  }
}

function clearGraph() {
  nodes = [];
  edgeMode = false;
  edgeStart = null;
  edgeCount = 0;
  edgeCount = 0;
  nodeHover = null;
  stillInNode = false;

  document.getElementById("node-count").innerHTML = nodes.length;
  document.getElementById("edge-count").innerHTML = edgeCount;
  document.getElementById("adjacency-list").innerHTML = "";
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

  nodeHover = nodeAtPoint(mouseX, mouseY, nodes);
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

  if (isDrawing && tool === toolModes.AREACOMPLETE) {
    drawPoints.push(new Point(mouseX, mouseY));
  }
}

function mouseUp() {
  let selectionArea = drawPoints.map((pt) => [pt.x, pt.y]);

  let selected = nodes.filter((n) => {
    let pt = [n.x, n.y];
    return inside(pt, selectionArea);
  });
  for (let i = 0; i < selected.length; i++) {
    for (let j = 0; j < selected.length; j++) {
      addEdgeEfficient(selected[i], selected[j]);
    }
  }

  if (tool === toolModes.AREACOMPLETE) {
    canvas.style.cursor = "url('images/area-complete-cursor.svg'), pointer";
  }

  setCommentary();
  isDrawing = false;
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

function setCommentary() {
  // number of nodes with at least 1 edge (often it's useful to ignore isolate nodes)
  let numConnected = nodes.filter((x) => x.neighbors.length > 0).length;

  let commentary = "Nice graph!"; 
  document.getElementById("commentary").innerHTML =
    "&#34;" + commentary + "&#34;";
}

function convertToAdjList(nodes) {
  let adjList = [];
  for (let i = 0; i < nodes.length; i++) {
    adjList.push([]);
    for (let j = 0; j < nodes[i].neighbors.length; j++) {
      adjList[i].push(nodes[i].neighbors[j].index);
    }
  }
  return adjList;
}

function addEdge(source, target) {
  addEdgeEfficient(source, target);
  setCommentary();
}

function getEdges(nodes) {
  let edges = [];
  let marked = new Set();
  for (let i = 0; i < nodes.length; i++) {
    marked.add(nodes[i]);
    for (let j = 0; j < nodes[i].neighbors.length; j++) {
      if (!marked.has(nodes[i].neighbors[j])) {
        edges.push([nodes[i], nodes[i].neighbors[j]]);
      }
    }
  }
  return edges;
}

function addEdgeEfficient(source, target) {
  if (target !== source && !source.neighbors.includes(target)) {
    source.neighbors.push(target);
    target.neighbors.push(source);
    source = target;
    edgeCount++;
    document.getElementById("edge-count").innerHTML = edgeCount;
    let adjList = document.getElementById("adjacency-list");
    if (adjList && adjList.hasChildNodes()) {
      let items = adjList.childNodes;
      let startIx = 0;
      let clickedIx = 0;
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i] === source) {
          startIx = i;
        }
        if (nodes[i] === target) {
          clickedIx = i;
        }
      }
      items[startIx].appendChild(document.createTextNode(" " + clickedIx));
      items[clickedIx].appendChild(document.createTextNode(" " + startIx));
    }
  }
}

// THANK YOU to https://stars.library.ucf.edu/cgi/viewcontent.cgi?referer=https://www.google.com/&httpsredir=1&article=1105&context=istlibrary
// This is based on the algorithm(s) described in the link above.
function isomorphism(g1, g2) {
  if (g1.length !== g2.length) {
    return false;
  } else {
    let perm = Array.from({ length: g1.length }).map((x) => -1);
    let used = Array.from({ length: g1.length }).map((x) => false);
    let level = g1.length - 1;
    return bruteForce(level, used, perm, g1, g2);
  }
}

function bruteForce(level, used, perm, g1, g2) {
  let result = false;

  if (level === -1) {
    result = checkEdges(perm, g1, g2);
  } else {
    let i = 0;
    while (i < g1.length && result === false) {
      if (used[i] === false) {
        used[i] = true;
        perm[level] = i;
        result = bruteForce(level - 1, used, perm, g1, g2);
        used[i] = false;
      }
      i = i + 1;
    }
  }
  return result;
}

// g1 and g2 are adjacency lists assumed to be the same length and are valid representations of bidirectional graphs
// perm is a mapping from nodes in g1 to g2. This function checks whether this mapping is a correct isomorphism between the two graphs
function checkEdges(perm, g1, g2) {
  for (let i = 0; i < g1.length; i++) {
    for (let j = 0; j < g1[i].length; j++) {
      let g1_target = g1[i][j];
      let g2_source = perm[i];
      let g2_target = perm[g1_target];
      let g2_all_targets = g2[g2_source];
      if (!g2_all_targets.includes(g2_target)) {
        return false;
      }
    }
    if (g1[i].length !== g2[perm[i]].length) {
      // just delete this if block and then the algorithm returns true if g1 is a subgraph of g2!
      return false;
    }
  }
  return true;
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
