const {
    Graph,
} = require("./graph.js");

// Returns String of graph types
function calculateGraphType(g) {
  let types = [];

  if (g.nodeCount === 1) {
    types.push("trivial");
  } else if (g.edgeCount === 0) {
    types.push("unconnected");
  } else {
    if (isComplete(g)) {
      types.push("complete");
    }
    if (isCycleGraph(g)) {
      types.push("cycle");
    }
    if (isButterflyGraph(g)) {
      types.push("butterfly");
    }
    if (isKayakPaddleGraph(g)) {
      types.push("kayak paddle");
    }
    if (isPaw(g)) {
      types.push("paw");
    }
  }

  return types;
}

// tells you if two graphs have an equivalent structure (i.e. they're the "same")
// THANK YOU to https://stars.library.ucf.edu/cgi/viewcontent.cgi?referer=https://www.google.com/&httpsredir=1&article=1105&context=istlibrary
// This is based on the algorithm(s) described in the link above.
// Note: graph isomorphism is a non-trivial problem, a quasipolynomial was only found in 2017 and I'm sure I couldn't implement it.
// this uses mostly brute force with basic pruning using simple invariants (e.g. degree sequence)
// for small graphs I tested it on, it worked fine
// will work poorly on graphs with lots of edges and with similar degree sequences
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

// TODO: uses recursion, maybe should be iterative to reduce memory in call stack?
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

// Complete graphs have (n*(n-1))/2 edges
function isComplete(graph) {
  return graph.edgeCount === (graph.nodeCount * (graph.nodeCount - 1)) / 2;
}

// The symbols K2,K3...Kn designate complete graphs of size n. This function generates a function to test if a graph is a complete graph of size n.
// this is somewhat abstract, but it cuts down on a lot of code repetition (you don't have to define different functions like isK3(..) isK4(...), etc.)
function completeGraphChecker(n) {
  return function (graph) {
    return graph.nodeCount === n && isComplete(graph);
  };
}

// Returns a function that checks if it's a *specific* cycle graph (say, C5).
function cycleGraphChecker(n) {
  return function (graph) {
    return graph.nodeCount === n && isCycleGraph(graph);
  };
}

function isCycleGraph(graph) {
    return isOnlyCycles(graph) && isOneCycle(graph);
}

// A graph is a 'cycle graph' if it has n nodes and n edges, and each node has degree 2.
// In other words, the graph is a big circle.
function isOnlyCycles(graph) {
  return (
    graph.nodeCount >= 3 &&
    graph.nodeCount === graph.edgeCount &&
    graph.nodeCount ===
      graph.getAdjList().filter((node) => node.length === 2).length
  );
}

// TODO (2025): Wait, what does this function do? How is it different from isOnlyCycles?
function isOneCycle(graph) {
  let adjList = graph.getAdjList();
  // starts at any connected node, walks edges exactly numConnected times. If it's a cycle graph, it should end up back at start without revisiting any nodes
  let start = adjList.findIndex((n) => n.length > 0);
  if (start == -1) { // TODO (2025) I didn't check if this is correct, we should add to tests
    return false;
  }
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
      graph.getAdjList().filter((x) => x.length === 1).length ===
        graph.nodeCount - 1
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

// returns a new Graph representing the connected component to that node; returns an empty graph if the node doesn't exist in the graph
// TODO: it's unclear if this copies the values stored at nodes or merely copies a reference
function getConnectedComponent(node, graph) {
  let visited = Array.from({ length: graph.nodeCount }).map((x) => false);
  if (!graph.nodeValues.has(node)) {
    return new Graph();
  } else {
    let curIndex = graph.nodeValues.get(node);
    memoizeCC(curIndex, graph.getAdjList(), visited);
    let connectedIndices = [];
    for (let i = 0; i < visited.length; i++) {
      if (visited[i]) {
        connectedIndices.push(i);
      }
    }
    return subGraph(connectedIndices, graph);
  }
}

// recursive helper for getConnectedComponent function
function memoizeCC(nodeIx, adjList, visited) {
  visited[nodeIx] = true;

  for (let i = 0; i < adjList[nodeIx].length; i++) {
    let targetIndex = adjList[nodeIx][i];
    if (!visited[targetIndex]) {
      memoizeCC(targetIndex, adjList, visited);
    }
  }
  return visited;
}

// for a given set of node indices, returns the subgraph that contains all those nodes
// if an index doesn't exist, it ignores it. (Maybe it should throw an error?)
function subGraph(nodeIndices, graph) {
  let newGraph = new Graph();
  for (const index of nodeIndices) {
    if (graph.indices.has(index)) {
      newGraph.addNode(graph.indices.get(index));
    }
  }
  for (const index of nodeIndices) {
    if (graph.indices.has(index)) {
      let sourceValue = graph.indices.get(index);
      let targets = graph.getNeighbors(sourceValue);
      let targetsToAdd = targets.filter((x) => newGraph.nodeValues.has(x));
      for (const targetValue of targetsToAdd) {
        newGraph.addEdge(sourceValue, targetValue);
      }
    }
  }
  return newGraph;
}

// returns a string representing the .DOT format of the graph for applications such as GraphViz
// adds "n" to the beginning of the node label names because .dot format won't accept digits at the start of a label name
// e.g. 3 gets turned into n3
// TODO have user label graphs however they want
function getDot(graph) {
  const edges = graph.getEdgeIndices();
  let ret = "graph {\n";
  for (const e of edges) {
    ret = ret + " n" + e[0] + " -- n" + e[1] + " \n";
  }

  const adjList = graph.getAdjList();
  for (let i=0; i<adjList.length; i++) {
    if (adjList[i].length == 0) {
      ret = ret + " n" + i + "\n";
    }
  }
  ret += "}";
  return ret;
}

exports.checkEdges = checkEdges;
exports.isomorphism = isomorphism;
exports.isComplete = isComplete;
exports.completeGraphChecker = completeGraphChecker;
exports.cycleGraphChecker = cycleGraphChecker;
exports.isPaw = isPaw;
exports.starGraphChecker = starGraphChecker;
exports.isKayakPaddleGraph = isKayakPaddleGraph;
exports.isButterflyGraph = isButterflyGraph;
exports.getConnectedComponent = getConnectedComponent;
exports.subGraph = subGraph;
exports.getDot = getDot;
