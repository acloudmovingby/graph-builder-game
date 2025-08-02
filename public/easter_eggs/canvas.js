let canvas = document.getElementById("canvas");
let eggsHtml = document.querySelectorAll(".egg");
const infoPaneWidth = 300; // TODO: don't set constant here, but get width from computed width of info pane 
let graph = new Graph();
let edgeMode = false;
let edgeStart = null;
let mouseX = 0;
let mouseY = 0;
let nodeHover = null;
let stillInNode = false; // true if mouse is still inside node bounds for a node that was just created, so you don't immediately get a hover effect after creating the node but only starts happening after you've left that node's location
let clearButtonHover = false;

let curNode = null;

const timeInit = new Date().getSeconds();
const nodeRadius = 15;

const easterEggState = {
  visible: false,
  eggs: [
    new Egg(
      "K3",
      "K3",
      completeGraphChecker(3),
      "You made a triangle! This is also a complete graph (K3) and a cycle graph (C3)!",
      "The complete graph K3 has 3 nodes and 3 edges.",
      "Complete Graph K3"
    ),
    new Egg(
      "K4",
      "K4",
      completeGraphChecker(4),
      "Wow! A complete graph!! This one's called K4. It forms the edge set of a tetrahedron, but you probably knew that already.",
      "The complete graph K4 has 4 nodes and 6 edges.",
      "Complete Graph K4"
    ),
    new Egg(
      "K5",
      "K5",
      completeGraphChecker(5),
      "From wikipedia: 'by Kuratowski's theorem, a graph is planar if and only if it contains neither K5 nor the complete bipartite graph K3,3.",
      "The complete graph K5 has 5 nodes and 10 edges. It looks like a pentagram.",
      "Complete Graph K5"
    ),
    new Egg(
      "K6",
      "K6",
      completeGraphChecker(6),
      "Wow! A complete graph!! This one's called K6. This beautiful graph, arranged on a hexagon, has appeared in many places across the world. Such a drawing is called a 'mystic rose'.",
      "The complete graph K6 has 6 nodes and 15 edges. It has appeared in art throughout the world.",
      "Complete Graph K6"
    ),
    new Egg(
      "C3",
      "C3",
      cycleGraphChecker(3),
      "You made a triangle! This is also a complete graph (K3) and a cycle graph (C3)!",
      "The cycle graph C3 has 3 nodes and 3 edges. It's the same as the complete graph K3.",
      "Cycle Graph C3"
    ),
    new Egg(
      "C4",
      "C4",
      cycleGraphChecker(4),
      "You've made C4, the cycle graph with 4 nodes! Well done!",
      "The cycle graph C4 has 4 nodes and 4 edges",
      "Cycle Graph C4"
    ),
    new Egg(
      "C5",
      "C5",
      cycleGraphChecker(5),
      "Ah! Cycle graph C5! An excellent choice!",
      "The cycle graph C5 has 5 nodes and 5 edges.",
      "Cycle Graph C5"
    ),
    new Egg(
      "C6",
      "C6",
      cycleGraphChecker(6),
      "C6. Beautiful. It's like 6 people holding hands in a circle. Maybe they're casting a spell or something, I don't know.",
      "The cycle graph C6 has 6 nodes and 6 edges.",
      "Cycle Graph C6"
    ),
    new Egg(
      "paw",
      "🐾",
      isPaw,
      "Rawr! I'm a paw graph!",
      "The paw graph has 4 nodes and 4 edges.",
      "The Paw Graph"
    ),
    new Egg(
      "claw",
      "S3",
      starGraphChecker(4),
      "This is the star graph S3, also known as the 'claw' graph. Note that in graph theory notation, the n in Sn refers to the number of spokes around the center. The total number of nodes, including the center is, is n+1. Graphs without the claw as a subgraph are known as 'claw-free graphs'.",
      "The 'claw' graph has 4 nodes and 3 edges. It is the star graph S3.",
      "Star Graph S3"
    ),
    new Egg(
      "S4",
      "S4",
      starGraphChecker(5),
      "Star graph S4! Star graphs are technically a special kind of tree. Stars are special kind of tree? Math is silly.",
      "The star graph S4 has 5 nodes and 4 edges.",
      "Star Graph S4"
    ),
    new Egg(
      "S5",
      "S5",
      starGraphChecker(6),
      "Star graph S5! According to wikipedia, the star network, a computer network modeled after the star graph, is important in distributed computing.",
      "The star graph S5 has 6 nodes and 5 edges.",
      "Star Graph S5"
    ),
    new Egg(
      "kayak",
      "🛶",
      isKayakPaddleGraph,
      "This one's called the kayak paddle graph. I swear, I'm not making this up.",
      "The kayak paddle graph has 6 nodes and 7 edges.",
      "Kayak Paddle Graph"
    ),
    new Egg(
      "butterfly",
      "🦋",
      isButterflyGraph,
      "The butterfly graph! Also known as the bowtie graph or the friendship graph, F2. An all around high-quality graph!",
      "The butterfly graph has 5 nodes and 6 edges. It's also known as the bowtie graph and the friendship graph F2.",
      "Butterfly Graph"
    ),
  ],
};

function Egg(id, symbol, isSubGraphOf, commentary, description, title) {
  this.discovered = false;
  this.id = id;
  this.symbol = symbol;
  this.isSubGraphOf = isSubGraphOf; // function that takes nodes as argument and returns boolean
  this.commentary = commentary ?? "default comment";
  this.description = description ?? "";
  this.title = title;
}

function refreshEasterEggs() {
  if (easterEggState.visible) {
    let easterEggs = document.getElementById("easter-eggs");
    let eggSlider = document.getElementById("egg-slider");
    if (easterEggs && eggSlider) {
      easterEggs.style.display = "block";
      eggSlider.style.display = "block";
    }
    easterEggState.eggs.forEach((egg) => {
      let htmlEgg = document.getElementById(egg.id);
      if (htmlEgg) {
        htmlEgg.style.color = egg.discovered ? "blue" : "black";
        htmlEgg.style.fontWeight = egg.discovered ? "bold" : "normal";
        htmlEgg.innerHTML = egg.discovered ? egg.symbol : "?";
      }
    });
  }
}

if (canvas.getContext) {
  canvas.addEventListener("mousedown", canvasClick, false);
  canvas.addEventListener("mousemove", mouseMove, false);
  canvas.addEventListener("mouseleave", mouseLeave, false);
  window.requestAnimationFrame(draw);
}

if (eggsHtml) {
  for (let i = 0; i < eggsHtml.length; i++) {
    eggsHtml[i].addEventListener(
      "mouseenter",
      (event) => {
        let egg = easterEggState.eggs.find((egg) => egg.id === eggsHtml[i].id);
        if (egg) {
          let hoverInfoElement = document.getElementById("hover-info-pane");
          hoverInfoElement.style.visibility = "visible";
          hoverInfoElement.style.left = `${
            event.x - hoverInfoElement.offsetWidth
          }px`;
          hoverInfoElement.style.top = `${event.y}px`;
          document.getElementById("hover-description").innerHTML = egg.discovered
            ? egg.description
            : "...not yet discovered...";
          document.getElementById("hover-header").innerHTML =
            egg.discovered ? egg.title : "???";
            document.getElementById("hover-info-img").src = egg.discovered ? `../images/${egg.id}.png` : "images/blank.png";
        }
      },
      false
    );
    eggsHtml[i].addEventListener(
      "mouseleave",
      (event) => {
        let hoverInfoElement = document.getElementById("hover-info-pane");
        hoverInfoElement.style.visibility = "hidden";
      },
      false
    );
  }
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

  let nodeClicked = nodeAtPoint(x, y, graph.getNodeValues());

  if (!edgeMode && !nodeClicked) {
    // create new Node
    let newNode = new NodeData(0, x, y);
    curNode = newNode;
    graph.addNode(newNode);
    stillInNode = true;
    document.getElementById("node-count").innerHTML = graph.nodeCount;
    setCommentary(graph);
  } else if (!edgeMode) {
    // start edge on the node clicked
    edgeMode = true;
    edgeStart = nodeClicked;
    curNode = nodeClicked;
  } else if (nodeClicked && nodeClicked != edgeStart) {
    // add edge
    if (!edgeStart.neighbors.includes(nodeClicked)) {
      edgeStart.neighbors.push(nodeClicked);//TODO get rid of neighbors entirely
      nodeClicked.neighbors.push(edgeStart);
      graph.addEdge(edgeStart, nodeClicked);

      edgeStart = nodeClicked;
      curNode = nodeClicked;

      document.getElementById("edge-count").innerHTML = graph.edgeCount;
      setCommentary(graph);
    }
    edgeStart = nodeClicked;
  } else {
    // exit edge mode
    edgeMode = false;
    edgeStart = null;
  }
}

function clearGraph() {
  edgeMode = false;
  edgeStart = null;
  nodeHover = null;
  stillInNode = false;
  graph = new Graph();
  document.getElementById("node-count").innerHTML = graph.nodeCount;
  document.getElementById("edge-count").innerHTML = graph.edgeCount;
  setCommentary(graph);
  refreshEasterEggs();
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
}

function setCommentary(graph) {
  let connected = getConnectedComponent(curNode, graph);

  let egg = easterEggState.eggs.find((egg) => {
    return egg.isSubGraphOf(connected);
  });

  if (egg) {
    if (egg.id === "K3") {
      easterEggState.eggs.find((egg) => egg.id === "C3").discovered = true;
    }
    let commentary = egg.commentary;
    easterEggState.visible = true;
    egg.discovered = true;
    refreshEasterEggs();
    document.getElementById("commentary").innerHTML =
      "&#34;" + commentary + "&#34;";
    return;
  }

  // Some if's are redundant and there's not a grand plan of the logic here other than: check easy stuff first and if the condition is true, change commentary and don't check anything else
  // Note: the sequence of some comments won't make sense if I later add deletion
  let commentary = "Nice graph!";
  if (graph.nodeCount === 0) {
    commentary = "...an empty void...";
  } else if (graph.nodeCount === 1) {
    commentary = "A lone wolf.";
  } else if (graph.nodeCount === 2 && graph.edgeCount === 0) {
    commentary = "Two lone wolves!";
  } else if (graph.nodeCount === 2 && graph.edgeCount === 1) {
    commentary = "Awwww, they're connected! Cute.";
  } else if (graph.nodeCount === 3 && graph.edgeCount === 1) {
    commentary = "Classic third wheel.";
  } else if (connected.nodeCount === 3 && connected.edgeCount === 2) {
    commentary =
      "This graph is called S2, a star graph. It's a bit boring. You can do better.";
  } else if (
    graph.nodeCount < 6 &&
    graph.nodeCount > 2 &&
    graph.edgeCount === 0
  ) {
    commentary = "Yo get some edges in there. Things be lookin sparse.";
  } else if (
    graph.nodeCount >= 6 &&
    graph.nodeCount < 15 &&
    graph.edgeCount === 0
  ) {
    commentary =
      "So...to make an edge click on a node and then, without dragging, click on another node.";
  } else if (
    graph.nodeCount > 3 &&
    graph.nodeCount < 15 &&
    graph.edgeCount < 3
  ) {
    commentary = "Still pretty sparse";
  } else if (connected.nodeCount === 4 && connected.edgeCount === 3) {
    commentary = "Try making a cycle.";
  } else if (connected.nodeCount === 7 && isComplete(graph)) {
    commentary =
      "Well done. You made K7. You have a lot of time on your hands. But no eggs for you.";
  } else if (isOnlyCycles(graph)) {
    let isCycle = isOneCycle(graph);
    commentary = isCycle
      ? "Cool cycle graph!"
      : "You got a couple of cycle graphs goin on.";
  } else if (
    connected.nodeCount + 1 === graph.nodeCount &&
    graph.nodeCount > 6
  ) {
    commentary = "So close...";
  } else if (connected.nodeCount === graph.nodeCount && graph.nodeCount > 6) {
    commentary = "Feelin connected!!";
  } else if (graph.nodeCount >= 65 && graph.edgeCount > 30) {
    commentary =
      "Are you actually trying to connect all those? Please don't. I was joking. To complete this graph would take at least 2,556 edges.";
  } else if (graph.nodeCount >= 65) {
    commentary =
      "So there's a MEGA EASTER EGG in this game. Hint: start making edges...";
  } else if (graph.nodeCount >= 60) {
    commentary = "This is a lot nodes. Are the animations still smooth? :) ";
  } else if (graph.nodeCount >= 50) {
    commentary = "Is your finger tired?";
  } else if (graph.nodeCount >= 40) {
    commentary = "Make the screen blue with nodes for all I care...";
  } else if (graph.nodeCount >= 35) {
    commentary =
      "That's a lot of nodes. Are you trying to break my program? 😈 Try your best...";
  } else if (graph.nodeCount >= 25) {
    commentary = "You're adding a lot of nodes.";
  }
  document.getElementById("commentary").innerHTML =
    "&#34;" + commentary + "&#34;";
}
