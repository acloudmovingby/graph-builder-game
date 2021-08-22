const {
  checkEdges,
  isomorphism,
  Graph,
  isComplete,
  completeGraphChecker,
  cycleGraphChecker,
  isPaw,
  starGraphChecker,
  isKayakPaddleGraph,
  isButterflyGraph,
  getConnectedComponent,
  subGraph
} = require("./public/graph_algs");

test("checks edges with empty graphs", () => {
  expect(checkEdges([], [], [])).toBe(true);
});

test("checks graphs with single nodes", () => {
  let g1 = [[]];
  let g2 = [[]];
  let perm = [0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks dissimilar graphs with 2/2 nodes, edges are 1/0", () => {
  let g1 = [[1], [0]];
  let g2 = [[], []];
  let perm = [1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  expect(checkEdges(perm, g2, g1)).toBe(false);
  perm = [0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  expect(checkEdges(perm, g2, g1)).toBe(false);
});

test("checks graphs with 2/2 nodes, no edges", () => {
  let g1 = [[], []];
  let g2 = [[], []];
  let perm = [1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks graphs with two nodes, one edge", () => {
  let g1 = [[1], [0]];
  let g2 = [[1], [0]];
  let perm = [1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks dissimilar graphs with two nodes", () => {
  let g1 = [[1], [0]];
  let g2 = [[], []];
  let perm = [1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("checks graphs with three nodes and zero edges", () => {
  let g1 = [[], [], []];
  let g2 = [[], [], []];
  let perm = [2, 1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [0, 1, 2];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks graphs with three nodes one edge, both correct and incorrect perm mappings", () => {
  let g1 = [[], [2], [1]];
  let g2 = [[1], [0], []];
  let perm = [2, 1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("checks graphs with three nodes, two edges, both correct and incorrect perm mappings", () => {
  let g1 = [[1], [0, 2], [1]];
  let g2 = [[1, 2], [0], [0]];
  let perm = [1, 0, 2];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [0, 1, 2];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("checks dissimilar graphs with three nodes", () => {
  let g1 = [
    [1, 2],
    [0, 2],
    [1, 0],
  ];
  let g2 = [[1, 2], [0], [0]];
  let perm = [1, 0, 2];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [2, 0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [0, 1, 2];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("checks graphs with three nodes and three edges", () => {
  let g1 = [
    [1, 2],
    [0, 2],
    [1, 0],
  ];
  let g2 = [
    [2, 1],
    [2, 0],
    [0, 1],
  ]; // note adj lists are identicaly, but order is not same
  let perm = [0, 1, 2];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [0, 2, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [1, 0, 2];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 0, 1];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 1, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks graphs with four nodes and four edges", () => {
  let g1 = [[1, 2], [0, 2], [0, 1, 3], [2]];
  let g2 = [[3], [2, 3], [1, 3], [0, 1, 2]];
  let perm = [1, 2, 3, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 1, 3, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [3, 1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [0, 1, 2, 3];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("checks graphs with four nodes and four edges", () => {
  let g1 = [[1, 2], [0, 2], [0, 1, 3], [2]];
  let g2 = [[3], [2, 3], [1, 3], [0, 1, 2]];
  let perm = [1, 2, 3, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [2, 1, 3, 0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
  perm = [3, 1, 2, 0];
  expect(checkEdges(perm, g1, g2)).toBe(false);
  perm = [0, 1, 2, 3];
  expect(checkEdges(perm, g1, g2)).toBe(false);
});

test("isomorphism - on empty graphs should be true (they're both empty sets)", () => {
  let g1 = [];
  let g2 = [];
  expect(isomorphism(g1, g2)).toBe(true);
});

test("isomorphism - simple fail with one node/zero node; commutative", () => {
  let g1 = [[]];
  let g2 = [];
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});

test("isomorphism - one node in each graph", () => {
  let g1 = [[]];
  let g2 = [[]];
  expect(isomorphism(g1, g2)).toBe(true);
  expect(isomorphism(g2, g1)).toBe(true);
});

test("isomorphism - 2/0 nodes, commutative ", () => {
  let g1 = [[], []];
  let g2 = [];
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});

test("isomorphism - 2/1 nodes, commutative ", () => {
  let g1 = [[], []];
  let g2 = [[]];
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});

test("isomorphism - test with isomorphic 2/2 nodes, 1/1 edges; commutative", () => {
  let g1 = [[1], [0]];
  let g2 = [[1], [0]];
  expect(isomorphism(g1, g2)).toBe(true);
  expect(isomorphism(g2, g1)).toBe(true);
});

test("isomorphism - test with non-isomorphic 2/2 nodes, 1/0 edges; commutative", () => {
  let g1 = [[1], [0]];
  let g2 = [[], []];
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});

test("isomorphism - test with isomorphic cycle graphs, 8/8 nodes, 16 edges; commutative", () => {
  let g1 = [
    [1, 7],
    [0, 2],
    [1, 3],
    [2, 4],
    [3, 5],
    [4, 6],
    [5, 7],
    [6, 0],
  ];
  let g2 = [
    [7, 2],
    [3, 5],
    [0, 6],
    [1, 4],
    [6, 3],
    [1, 7],
    [2, 4],
    [5, 0],
  ];
  expect(isomorphism(g1, g2)).toBe(true);
  expect(isomorphism(g2, g1)).toBe(true);
});

test("isomorphism - test with isomorphic 2 separate cycle graphs, 8/8 nodes, 16 edges; commutative", () => {
  let g1 = [
    [1, 7],
    [0, 2],
    [1, 3],
    [2, 4],
    [3, 5],
    [4, 6],
    [5, 7],
    [6, 0],
  ];
  let g2 = [
    [1, 7],
    [0, 2],
    [1, 3],
    [2, 4],
    [3, 5],
    [4, 6],
    [5, 7],
    [6, 0],
  ];
  expect(isomorphism(g1, g2)).toBe(true);
  expect(isomorphism(g2, g1)).toBe(true);
});

test("isomorphism - test with non-isomorphic 8/8 nodes, 16 edges; commutative", () => {
  let g1 = [
    [1, 7],
    [0, 2],
    [1, 3],
    [2, 4],
    [3, 5],
    [4, 6],
    [5, 7],
    [6, 0],
  ]; // big ring
  let g2 = [
    [7, 1],
    [0, 5],
    [6, 4],
    [4, 6],
    [3, 2],
    [7, 1],
    [3, 2],
    [5, 0],
  ]; // two non-overlapping rings
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});

test("isomorphism - test on nearly complete graph, 5 nodes; commutative", () => {
  let g1 = [
    [1, 2, 3, 4],
    [2, 0, 4],
    [3, 1, 0, 4],
    [4, 2, 0],
    [3, 0, 2, 1],
  ];
  let g2 = [
    [1, 2, 3],
    [2, 0, 3, 4],
    [3, 1, 0, 4],
    [4, 2, 1, 0],
    [3, 2, 1],
  ];
  expect(isomorphism(g1, g2)).toBe(true);
  expect(isomorphism(g2, g1)).toBe(true);
});

/******** */

test("Graph - empty graph; tests equality of structure not labels", () => {
  let g1 = new Graph();
  let adjList1 = g1.getAdjList();
  let adjList2 = [];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - single node; tests equality of structure not labels", () => {
  let g1 = new Graph();
  g1.addNode("A");
  let adjList1 = g1.getAdjList();
  let adjList2 = [[]];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - two nodes, no edges; tests equality of structure not labels", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  let adjList1 = g1.getAdjList();
  let adjList2 = [[], []];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - two nodes, one edge; tests equality of structure not labels", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addEdge("A", "B");
  let adjList1 = g1.getAdjList();
  let adjList2 = [[1], [0]];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - three nodes, two edges; tests equality of structure not labels", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addEdge("A", "B");
  g1.addNode("C");
  g1.addEdge("B", "C");
  let adjList1 = g1.getAdjList();
  let adjList2 = [[1], [0, 2], [1]];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - many nodes, complex structure; tests equality of structure not labels", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  g1.addNode("F");
  g1.addNode("G");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("D", "E");
  g1.addEdge("E", "F");
  g1.addEdge("F", "G");
  g1.addEdge("D", "F");
  let adjList1 = g1.getAdjList();
  let adjList2 = [[4, 6], [3, 2], [1, 3], [5, 1, 2], [0], [3], [0]];
  expect(isomorphism(adjList1, adjList2)).toBe(true);
});

test("Graph - tests edgecount is correct, simple case", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addEdge("A","B");
  g1.addEdge("C","B");
  expect(g1.edgeCount).toBe(2);
});

test("isComplete - tests empty graph, returns true", () => {
  let g1 = new Graph();
  expect(isComplete(g1)).toBe(true);
});

test("isComplete - tests one node graph, returns true", () => {
  let g1 = new Graph();
  g1.addNode("A");
  expect(isComplete(g1)).toBe(true);
});

test("isComplete - tests two node graphs", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("A", "B");
  expect(isComplete(g1)).toBe(true);
});

test("isComplete - tests three node graphs", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("A", "B");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("B", "C");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("A", "C");
  expect(g1.nodeCount).toBe(3);
  expect(g1.edgeCount).toBe(3);
  expect(isComplete(g1)).toBe(true);
});

test("completeGraphChecker - single node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  expect(completeGraphChecker(1)(g1)).toBe(true);
  expect(completeGraphChecker(0)(g1)).toBe(false);
  expect(completeGraphChecker(2)(g1)).toBe(false);
});

test("completeGraphChecker - two node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  expect(completeGraphChecker(1)(g1)).toBe(false);
  expect(completeGraphChecker(2)(g1)).toBe(false);
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge();
});

test("completeGraphChecker - three node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  expect(completeGraphChecker(1)(g1)).toBe(false);
  expect(completeGraphChecker(2)(g1)).toBe(false);
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("A", "B");
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("B", "C");
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("A", "C");
  expect(completeGraphChecker(3)(g1)).toBe(true);
});

test("completeGraphChecker - four node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  expect(completeGraphChecker(1)(g1)).toBe(false);
  expect(completeGraphChecker(3)(g1)).toBe(false);
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("A", "B");
  expect(g1.edgeCount).toBe(1);
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("B", "C");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("C", "D");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("A", "D");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("B", "D");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("A", "C");
  expect(g1.edgeCount).toBe(6);
  expect(completeGraphChecker(4)(g1)).toBe(true);
});

test("cycleGraphChecker - three nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "A");
  expect(cycleGraphChecker(3)(g1)).toBe(true);
});

test("cycleGraphChecker - four nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "D");
  g1.addEdge("A", "D");
  expect(cycleGraphChecker(4)(g1)).toBe(true);
});

test("cycleGraphChecker - five nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "D");
  g1.addEdge("D", "E");
  g1.addEdge("A", "E");
  expect(cycleGraphChecker(5)(g1)).toBe(true);
});

test("isPaw test", () => {
  let g1 = new Graph();
  expect(isPaw(g1)).toBe(false);
  g1.addNode("A");
  expect(isPaw(g1)).toBe(false);
  g1.addNode("B");
  expect(isPaw(g1)).toBe(false);
  g1.addNode("C");
  expect(isPaw(g1)).toBe(false);
  g1.addNode("D");
  expect(isPaw(g1)).toBe(false);
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("A", "C");
  expect(isPaw(g1)).toBe(false);
  g1.addEdge("B", "D");
  expect(isPaw(g1)).toBe(true);
  g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "D");
  g1.addEdge("A", "D");
  expect(isPaw(g1)).toBe(false);
});

test("starGraphChecker - 3 nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  expect(starGraphChecker(2)(g1)).toBe(false);
  expect(starGraphChecker(3)(g1)).toBe(true);
  expect(starGraphChecker(4)(g1)).toBe(false);
});

test("starGraphChecker - 4 nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addEdge("A", "B");
  g1.addEdge("A", "C");
  expect(starGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("A", "D");
  expect(starGraphChecker(4)(g1)).toBe(true);
});

test("starGraphChecker - 5 nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  g1.addEdge("A", "B");
  g1.addEdge("A", "C");
  g1.addEdge("A", "D");
  expect(starGraphChecker(5)(g1)).toBe(false);
  g1.addEdge("A", "E");
  expect(starGraphChecker(5)(g1)).toBe(true);
});

test("kayakPaddleGraph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  g1.addNode("F");
  expect(isKayakPaddleGraph(g1)).toBe(false);
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "A");
  g1.addEdge("D", "E");
  g1.addEdge("E", "F");
  g1.addEdge("D", "F");
  expect(isKayakPaddleGraph(g1)).toBe(false);
  g1.addEdge("A", "D");
  expect(isKayakPaddleGraph(g1)).toBe(true);
});

test("isButterflyGraph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  expect(isButterflyGraph(g1)).toBe(false);
  g1.addEdge("A", "B");
  g1.addEdge("B", "C");
  g1.addEdge("C", "A");
  g1.addEdge("C", "D");
  g1.addEdge("D", "E");
  g1.addEdge("E", "C");
  expect(isButterflyGraph(g1)).toBe(true);
});

test("getEdges", () => {
  let g1 = new Graph();
  expect(g1.getEdges().length).toBe(0);
  g1.addNode("A");
  g1.addNode("B");
  expect(g1.getEdges().length).toBe(0);
  g1.addEdge("A", "B");
  expect(g1.getEdges().length).toBe(1);
  g1.addNode("C");
  g1.addEdge("A", "C");
  expect(g1.getEdges().length).toBe(2);
});

test("getNeighbors - simple", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  expect(g1.getNeighbors("A").length).toBe(0);
  g1.addEdge("A","B");
  expect(g1.getNeighbors("A").length).toBe(1);
  expect(g1.getNeighbors("B").length).toBe(1);
});

test("getNeighbors - more complex graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  expect(g1.getNeighbors("A").length).toBe(0);
  g1.addEdge("A","B");
  g1.addEdge("A","C");
  g1.addEdge("A","D");
  g1.addEdge("B","C");
  expect(g1.getNeighbors("A").length).toBe(3);
  expect(g1.getNeighbors("B").length).toBe(2);
  expect(g1.getNeighbors("C").length).toBe(2);
  expect(g1.getNeighbors("D").length).toBe(1);
  expect(g1.getNeighbors("D").includes("A")).toBe(true);
});

test("subGraph - empty graph", () => {
  let g1 = new Graph();
  let g2 = subGraph([],g1);
  expect(g2.getAdjList.length).toBe(0);
  g2 = subGraph([1],g1); // if the index doesn't exist, subgraph returns empty graph
  expect(g2.getAdjList.length).toBe(0);
});

test("subGraph - one node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  let g2 = subGraph([0],g1);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(true);
  expect(g2.nodeValues.keys().next().value).toBe("A");
});

test("subGraph - two node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  let sub2 = subGraph([0],g1);
  let sub3 = subGraph([1],g1);
  expect(isomorphism(sub2.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(sub3.getAdjList(),[[]])).toBe(true);
  let sub4 = subGraph([0,1],g1);
  expect(isomorphism(sub4.getAdjList(),[[],[]])).toBe(true);
  g1.addEdge("A","B");
  let sub5 = subGraph([0,1],g1);
  expect(isomorphism(sub5.getAdjList(),[[1],[0]])).toBe(true);
});

test("subGraph - more complex graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addNode("D");
  g1.addNode("E");
  g1.addEdge("A","B");
  g1.addEdge("B","C");
  g1.addEdge("A","C");
  g1.addEdge("A","D");
  let sub = subGraph([0,3,4],g1); // nodes A, D, E
  let expected = [[1],[0],[]];
  expect(isomorphism(sub.getAdjList(),expected)).toBe(true);
});

test("getConnectedComponent - empty graph", () => {
  let g1 = new Graph();
  let fakeNode = "A";
  let connectedComponent = getConnectedComponent(fakeNode,g1);
  expect(isomorphism(connectedComponent.getAdjList(),g1.getAdjList())).toBe(true);
});

test("getConnectedComponent - one node", () => {
  let g1 = new Graph();
  g1.addNode("A");
  let connected1 = getConnectedComponent("A",g1);
  let connected2 = getConnectedComponent("B",g1); // "fake" node; doesn't exist in graph
  expect(isomorphism(connected1.getAdjList(),g1.getAdjList())).toBe(true);
  expect(isomorphism(connected2.getAdjList(),g1.getAdjList())).toBe(false);
});

test("getConnectedComponent - two nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  let connected1 = getConnectedComponent("A",g1);
  let connected2 = getConnectedComponent("B",g1); 
  expect(isomorphism(connected1.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(connected2.getAdjList(),[[]])).toBe(true);
  g1.addEdge("A","B");
  connected1 = getConnectedComponent("A",g1);
  expect(isomorphism(connected1.getAdjList(),g1.getAdjList())).toBe(true);
});

test("getConnectedComponent - three nodes", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addEdge("A","B");
  let connected1 = getConnectedComponent("A",g1);
  let connected2 = getConnectedComponent("C",g1); 
  expect(isomorphism(connected1.getAdjList(),[[1],[0]])).toBe(true);
  expect(isomorphism(connected2.getAdjList(),[[]])).toBe(true);
});

test("Clone - Tests deep copy of graph structure (not contents) - empty", () => {
  let g1 = new Graph();
  let g2 = g1.clone((x) => x);

  expect(isomorphism(g1.getAdjList(),[])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(true);
});

test("Clone - Tests deep copy of graph structure (not contents) - add one node original", () => {
  let g1 = new Graph();
  let g2 = g1.clone((x) => x);

  // alter original graph, make sure it doesn't affect clone
  g1.addNode("A");
  expect(isomorphism(g1.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(false);
});

test("Clone - Tests deep copy of graph structure (not contents) - add one node clone", () => {
  let g1 = new Graph();
  let g2 = g1.clone((x) => x);

  // alter clone graph, make sure it doesn't affect original
  g2.addNode("A");
  expect(isomorphism(g2.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),[])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(false);
});

test("Clone - Tests deep copy of graph structure (not contents) - test cloning single node", () => {
  let g1 = new Graph();
  g1.addNode("A");
  let g2 = g1.clone((x) => x);

  expect(isomorphism(g2.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(true);

  g1.addNode("B");
  expect(isomorphism(g2.getAdjList(),[[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),[[],[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(false);
});

test("Clone - Tests deep copy of graph structure (not contents) - test cloning single edge", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addEdge("A","B");
  let g2 = g1.clone((x) => x);

  expect(isomorphism(g1.getAdjList(),[[1],[0]])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[[1],[0]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(true);

  g1.addNode("C");
  expect(isomorphism(g1.getAdjList(),[[1],[0],[]])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[[1],[0]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(false);
});

test("Clone - Tests deep copy of graph structure (not contents) - test cloning mulitple edges", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  g1.addEdge("A","B");
  g1.addEdge("B","C");
  g1.addNode("D");
  let g2 = g1.clone((x) => x);

  expect(isomorphism(g1.getAdjList(),[[1],[0,2],[1],[]])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[[1],[0,2],[1],[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(true);

  g1.addNode("E");
  g1.addNode("F");
  g1.addEdge("F","E");

  expect(isomorphism(g1.getAdjList(),[[1],[0,2],[1],[],[5],[4]])).toBe(true);
  expect(isomorphism(g2.getAdjList(),[[1],[0,2],[1],[]])).toBe(true);
  expect(isomorphism(g1.getAdjList(),g2.getAdjList())).toBe(false);
});

