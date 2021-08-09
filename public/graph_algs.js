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

class Graph {
  constructor() {
    this.adjList = [];
    this.nodeCount = 0;
    this.edgeCount = 0;
    this.directedEdgeCount = 0;
    this.labels = new Map(); // maps "labels" to their indices in the adjacency list. The labels are the values stored in each node as given by the user in addNode
  }

  addNode(nodeValue) {
    this.adjList.push([]);
    this.labels.set(nodeValue, this.adjList.length - 1);
    this.nodeCount++;
  }

  // returns true only if the graph contains these nodes already and the edge didn't already exist; does NOT allow parallel edges
  addEdge(nodeValue1, nodeValue2) {
    let containsNodes =
      this.labels.has(nodeValue1) && this.labels.has(nodeValue2);

    if (containsNodes) {
      let index1 = this.labels.get(nodeValue1);
      let index2 = this.labels.get(nodeValue2);
      let addedEdge = false;

      if (!this.adjList[index1].includes(index2)) {
        this.adjList[index1].push(index2);
        addedEdge = true;
      }

      if (!this.adjList[index2].includes(index1)) {
        this.adjList[index2].push(index1);
        addedEdge = true;
      }

      if (addedEdge) {
        this.edgeCount++;
        this.directedEdgeCount += 2;
      }
    }
    
  }

  // returns adjacency list as just indices (the pure structure of the graph without the values it stores)
  getAdjList() {
    return this.adjList;
  }
}

function isComplete(graph) {
  return graph.edgeCount === (graph.nodeCount * (graph.nodeCount - 1)) / 2;
}

// The symbols K2,K3...Kn designate complete graphs of size n. This function generates an algorithm to test if a graph is a complete graph of size n.
// this is somewhat abstract, but it cuts down on a lot of code repetition (you don't have to define different functions like isK3(..) isK4(...), etc.)
function completeGraphChecker(n) {
  return function (graph) {
    return graph.nodeCount === n && isComplete(graph);
  };
}

function cycleGraphChecker(n) {
  return function (graph) {
    return graph.nodeCount === n && isOnlyCycles(graph) && isOneCycle(graph);
  };
}

function isOnlyCycles(graph) {
  return (
    graph.nodeCount >= 3 &&
    graph.nodeCount === graph.edgeCount &&
    graph.nodeCount === graph.getAdjList().filter((node) => node.length === 2).length
  );
}

function isOneCycle(graph) {
  let adjList = graph.getAdjList();
  // starts at any connected node, walks edges exactly numConnected times. If it's a cycle graph, it should end up back at start without revisiting any nodes
  let start = adjList.findIndex((n) => n.length > 0);
  let cur = start;
  let visited = Array.from({ length: adjList.length }).map((x) => false);
  let isCycle = true;
  for (let i = 0; i < adjList.length; i++) {
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

function isPaw(graph) {
  return (
    graph.nodeCount === 4 &&
    graph.edgeCount === 4 &&
    graph.getAdjList().filter((x) => x.length === 3).length === 1
  );
}

function starGraphChecker(n) {
  return function (graph) {
    return (
      graph.nodeCount === n &&
      graph.getAdjList().some((x) => x.length === n - 1) &&
      graph.getAdjList().filter((x) => x.length === 1).length === graph.nodeCount - 1
    );
  };
}

function isKayakPaddleGraph(graph) {
    if (graph.nodeCount != 6) {
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
    return isomorphism(kpg, graph.getAdjList(graph));
}

function isButterflyGraph(graph) {
    if (graph.nodeCount != 5) {
      return false;
    } else {
      let bfg = [
        [1, 2],
        [2, 0],
        [3, 4, 1, 0],
        [2, 4],
        [3, 2],
      ];
      return isomorphism(bfg, graph.getAdjList());
    }
}

exports.checkEdges = checkEdges;
exports.isomorphism = isomorphism;
exports.Graph = Graph;
exports.isComplete = isComplete;
exports.completeGraphChecker = completeGraphChecker;
exports.cycleGraphChecker = cycleGraphChecker; 
exports.isPaw = isPaw;
exports.starGraphChecker = starGraphChecker;
exports.isKayakPaddleGraph = isKayakPaddleGraph;
exports.isButterflyGraph = isButterflyGraph;

