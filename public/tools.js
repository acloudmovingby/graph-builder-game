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

const buttonRadius = 18;
let toolButton1x = 150 + buttonRadius/2;
let toolButton1y = 40 + buttonRadius/2;
let toolOneMode = true;

const timeInit = new Date().getSeconds();
const nodeRadius = 15;

if (canvas.getContext) {
  canvas.addEventListener("mousedown", canvasClick, false);
  canvas.addEventListener("mousemove", mouseMove, false);
  canvas.addEventListener("mouseleave", mouseLeave, false);
  window.requestAnimationFrame(draw);
}

function isComplete(nodes) {
  let numConnected = nodes.filter((node) => node.neighbors.length > 0).length;
  return edgeCount === (numConnected * (numConnected - 1)) / 2;
}

// The symbols K2,K3...Kn designate complete graphs of size n. This function generates an algorithm to test if a graph is a complete graph of size n.
// this is somewhat abstract, but it cuts down on a lot of code repetition (you don't have to define different functions like isK3(..) isK4(...), etc.)
function completeGraphChecker(n) {
  return function (nodes) {
    let numConnected = nodes.filter((node) => node.neighbors.length > 0).length;
    return numConnected === n && isComplete(nodes);
  };
}

function cycleGraphChecker(n) {
  return function (nodes) {
    let numConnected = nodes.filter((node) => node.neighbors.length > 0).length;
    return numConnected === n && isOnlyCycles(nodes) && isOneCycle(nodes);
  };
}

function isOnlyCycles(nodes) {
  let numConnected = nodes.filter((node) => node.neighbors.length > 0).length;
  return (
    numConnected >= 3 &&
    numConnected === edgeCount &&
    numConnected === nodes.filter((node) => node.neighbors.length === 2).length
  );
}

function isOneCycle(nodes) {
  let adjList = convertToAdjList(nodes);
  let numConnected = adjList.filter((x) => x.length > 0).length;
  // starts at any connected node, walks edges exactly numConnected times. If it's a cycle graph, it should end up back at start without revisiting any nodes
  let start = adjList.findIndex((n) => n.length > 0);
  let cur = start;
  let visited = Array.from({ length: adjList.length }).map((x) => false);
  let isCycle = true;
  for (let i = 0; i < numConnected; i++) {
    visited[cur] = true;
    let neighbor0 = adjList[cur][0];
    let neighbor1 = adjList[cur][1];
    if (visited[neighbor0] && !visited[neighbor1]) {
      cur = neighbor1;
    } else if (!visited[neighbor0] && visited[neighbor1]) {
      cur = neighbor0;
    } else if (!visited[neighbor0] && !visited[neighbor1]) {
      cur = neighbor1;
    } else if (neighbor0 === start || neighbor1 === start) {
      cur = start;
    } else {
      isCycle = false;
      break;
    }
  }
  return isCycle && cur === start;
}

function isPaw(nodes) {
  let numConnected = nodes.filter((x) => x.neighbors.length > 0).length;
  return (
    numConnected === 4 &&
    edgeCount === 4 &&
    nodes.filter((x) => x.neighbors.length === 3).length === 1
  );
}

// returns an algorithm that checks if the graph is a so-called "star" graph with n+1 nodes (denoted Sn). The n refers to the number of spokes around the center.
function starGraphChecker(n) {
  return function (nodes) {
    let numConnected = nodes.filter((x) => x.neighbors.length > 0).length;
    return (
      numConnected === n &&
      nodes.some((x) => x.neighbors.length === n - 1) &&
      nodes.filter((x) => x.neighbors.length === 1).length === numConnected - 1
    );
  };
}

function isKayakPaddleGraph() {
  return function (nodes) {
    if (nodes.length != 6) {
      return false;
    }
    let kpg = [
      [1, 2],
      [2, 0],
      [3, 1, 0],
      [4, 5, 2],
      [3, 5],
      [3, 4],
    ];
    return isomorphism(kpg, convertToAdjList(nodes));
  };
}

function isButterflyGraph() {
  return function (nodes) {
    if (nodes.length != 5) {
      return false;
    } else {
      let bfg = [
        [1, 2],
        [2, 0],
        [3, 4, 1, 0],
        [2, 4],
        [3, 2],
      ];
      return isomorphism(bfg, convertToAdjList(nodes));
    }
  };
}

function drawButton(x, y, ctx) {
  ctx.beginPath();
  ctx.fillStyle = "white";
  ctx.arc(x, y, buttonRadius, 0, Math.PI * 2, false);
  ctx.fill();
  ctx.closePath();
}

function draw() {
  if (canvas.getContext) {
    let ctx = canvas.getContext("2d");
    ctx.canvas.width = window.innerWidth - infoPaneWidth;
    ctx.canvas.height = window.innerHeight;
    ctx.clearRect(0, 0, window.innerWidth * 2, window.innerHeight * 2);

    // temp tool buttons
    ctx.beginPath();
    ctx.fillStyle = "white";
    ctx.arc(150, 40, 18, 0, Math.PI * 2, false);
    ctx.fill();

    ctx.beginPath();
    ctx.fillStyle = "white";
    ctx.arc(200, 40, 18, 0, Math.PI * 2, false);
    ctx.fill();

    ctx.beginPath();
    ctx.fillStyle = "white";
    ctx.arc(250, 40, 18, 0, Math.PI * 2, false);
    ctx.fill();

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
  
  if (toolOneHover) {
    toolOneMode = true;
    console.log("clicked toolOne");
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
  //document.getElementById("adjacency-list").innerHTML = "";
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

  // tool one hover
  let dx = mouseX - toolButton1x;
  let dy = mouseY - toolButton1y;
  let distFromCent = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
  if (distFromCent < buttonRadius) {
    toolOneHover = true;
  }
}

function setCommentary() {
  // number of nodes with at least 1 edge (often it's useful to ignore isolate nodes)
  let numConnected = nodes.filter((x) => x.neighbors.length > 0).length;

  // Some if's are redundant and there's not a grand plan of the logic here other than: check easy stuff first and if the condition is true, change commentary and don't check anything else
  // The sequence of some comments won't make sense if I later add deletion
  let commentary = "Nice graph!";
  if (nodes.length === 0) {
    commentary = "...an empty void...";
  } else if (nodes.length === 1) {
    commentary = "A lone wolf.";
  } else if (nodes.length === 2 && edgeCount === 0) {
    commentary = "Two lone wolves!";
  } else if (nodes.length === 2 && edgeCount === 1) {
    commentary = "Awwww, they're connected! Cute.";
  } else if (nodes.length === 3 && edgeCount === 1) {
    commentary = "Classic third wheel situation.";
  } else if (numConnected === 3 && edgeCount === 2) {
    commentary =
      "This graph has many equivalent names, including S2, a star graph. It's a bit boring. You can do better.";
  } else if (nodes.length < 6 && nodes.length > 2 && edgeCount === 0) {
    commentary = "Yo get some edges in there. Things be lookin sparse.";
  } else if (nodes.length >= 6 && nodes.length < 15 && edgeCount === 0) {
    commentary =
      "So...to make an edge click on a node and then, without dragging, click on another node.";
  } else if (nodes.length > 3 && nodes.length < 15 && edgeCount < 3) {
    commentary = "Still pretty sparse";
  } else if (numConnected === 4 && edgeCount === 3) {
    commentary = "Try making a cycle.";
  } else if (numConnected === 7 && isComplete(nodes)) {
    commentary = "Well done. You made K7. The complete graph of 7 nodes.";
  } else if (isOnlyCycles(nodes)) {
    let isCycle = isOneCycle(nodes);
    commentary = isCycle
      ? "Cool cycle graph!"
      : "You got a couple of cycle graphs goin on.";
  } else if (numConnected + 1 === nodes.length && nodes.length > 6) {
    commentary = "So close...";
  } else if (numConnected === nodes.length && nodes.length > 6) {
    commentary = "Feelin connected!!";
  } else if (nodes.length >= 70 && edgeCount > 30) {
    commentary =
      "Are you actually trying to connect all those? Please don't. I was joking. To complete this graph would take at least 2,556 edges.";
  } else if (nodes.length >= 70) {
    commentary =
      "So there's a MEGA EASTER EGG in this game. Hint: start making edges...";
  } else if (nodes.length >= 60) {
    commentary = "Are the animations still smooth? I bet they are :) ";
  } else if (nodes.length >= 50) {
    commentary = "Yeah it is.";
  } else if (nodes.length >= 40) {
    commentary = "Is your finger tired?";
  } else if (nodes.length >= 30) {
    commentary = "Make the screen blue with nodes for all I care...";
  } else if (nodes.length >= 20) {
    commentary =
      "That's a lot of nodes. Are you trying to break my program? 😈 Try your best, I dare you.";
  } else if (nodes.length >= 15) {
    commentary = "You're adding a lot of nodes.";
  }
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
