let canvas = document.getElementById("canvas");
const infoPaneWidth = 400; // this MUST match the width of the info-pane set out in css
let nodes = [];
let edgeMode = false;
let edgeStart = null;
let edgeCount = 0;
let mouseX = 0;
let mouseY = 0;
let nodeHover = null;
let stillInNode = false; // true if mouse is still inside node bounds for a node that was just created. helps it so the hover effect doesn't happen immediately after adding node
let clearButtonHover = false;

const timeInit = new Date().getSeconds();
const nodeRadius = 15;

function refreshState() {
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
}

if (canvas.getContext) {
  canvas.addEventListener("mousedown", canvasClick, false);
  canvas.addEventListener("mousemove", mouseMove, false);
  canvas.addEventListener("mouseleave", mouseLeave, false);
  window.requestAnimationFrame(draw);
}

function canvasClick(event) {
  let canvasBounds = canvas.getBoundingClientRect();
  let x = event.x - canvasBounds.left;
  let y = event.y - canvasBounds.top;

  if (clearButtonHover) {
    refreshState();
    return;
  }

  let nodeClicked = nodeAtPoint(x, y, nodes);

  if (!edgeMode && !nodeClicked) {
    // create new Node
    nodes.push(new Node(nodes.length, 0, x, y));
    let node = document.createElement("LI");
    node.appendChild(document.createTextNode(nodes.length + ": "));
    document.getElementById("adjacency-list").appendChild(node);
    stillInNode = true;
    document.getElementById("node-count").innerHTML = nodes.length;
  } else if (!edgeMode) {
    // start edge on the node clicked
    edgeMode = true;
    edgeStart = nodeClicked;
  } else if (nodeClicked && nodeClicked != edgeStart) {
    if (!edgeStart.neighbors.includes(nodeClicked)) {
      edgeStart.neighbors.push(nodeClicked);
      nodeClicked.neighbors.push(edgeStart);
      // update adjacency list on webpage
      let adjList = document.getElementById("adjacency-list");
      if (adjList.hasChildNodes()) {
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
        edgeStart = nodeClicked;
        edgeCount++;
        document.getElementById("edge-count").innerHTML = edgeCount;
        items[startIx].appendChild(document.createTextNode(" " + clickedIx));
        items[clickedIx].appendChild(document.createTextNode(" " + startIx));
      }
    }
    edgeStart = nodeClicked;
  } else {
    // cancel edge mode
    edgeMode = false;
    edgeStart = null;
  }
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
    ctx.fillText(
      "clear",
      35,
      35
    );

    //edge mode, draw edge from edgeStart to mouse cursor
    if (edgeMode) {
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

    

    // message box text
    /*
    let boxX = (ctx.canvas.width - infoPaneWidth) / 2;
    let boxY = ctx.canvas.height - 100;
    let boxWidth = 300;
    let boxHeight = 35;
    ctx.fillStyle = "white";
    ctx.fillRect(
      boxX,
      boxY,
      boxWidth,
      boxHeight
    );
    ctx.font = "15px Arial";
    ctx.fillStyle = "black";
    ctx.textAlign = "center";
    ctx.fillText(
      "Not allowed",
      boxX + boxWidth/2,
      boxY + boxHeight/2
    );*/
  }
  window.requestAnimationFrame(draw);
}

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
