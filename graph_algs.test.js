const { checkEdges, isomorphism, Graph, isComplete, completeGraphChecker } = require("./public/graph_algs");

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
    let g1 = [[1,7], [0,2],[1,3],[2,4],[3,5],[4,6],[5,7],[6,0]];
    let g2 = [[7,2], [3,5],[0,6],[1,4],[6,3],[1,7],[2,4],[5,0]];
    expect(isomorphism(g1, g2)).toBe(true);
    expect(isomorphism(g2, g1)).toBe(true);
  });

  test("isomorphism - test with isomorphic 2 separate cycle graphs, 8/8 nodes, 16 edges; commutative", () => {
    let g1 = [[1,7], [0,2],[1,3],[2,4],[3,5],[4,6],[5,7],[6,0]];
    let g2 = [[1,7], [0,2],[1,3],[2,4],[3,5],[4,6],[5,7],[6,0]];
    expect(isomorphism(g1, g2)).toBe(true);
    expect(isomorphism(g2, g1)).toBe(true);
  });

  test("isomorphism - test with non-isomorphic 8/8 nodes, 16 edges; commutative", () => {
    let g1 = [[1,7], [0,2],[1,3],[2,4],[3,5],[4,6],[5,7],[6,0]]; // big ring
    let g2 = [[7,1], [0,5],[6,4],[4,6],[3,2],[7,1],[3,2],[5,0]]; // two non-overlapping rings
    expect(isomorphism(g1, g2)).toBe(false);
    expect(isomorphism(g2, g1)).toBe(false);
  });

  test("isomorphism - test on nearly complete graph, 5 nodes; commutative", () => {
    let g1 = [[1,2,3,4], [2,0,4],[3,1,0,4],[4,2,0],[3,0,2,1]]; 
    let g2 = [[1,2,3], [2,0,3,4],[3,1,0,4],[4,2,1,0],[3,2,1]];
    expect(isomorphism(g1, g2)).toBe(true);
    expect(isomorphism(g2, g1)).toBe(true);
  });

  /******** */

  test("Graph - empty graph; tests equality of structure not labels", () => {
    let g1 = new Graph();
    let adjList1 = g1.getAdjList();
    let adjList2 = [];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
  });

  test("Graph - single node; tests equality of structure not labels", () => {
    let g1 = new Graph();
    g1.addNode("A");
    let adjList1 = g1.getAdjList();
    let adjList2 = [[]];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
  });

  test("Graph - two nodes, no edges; tests equality of structure not labels", () => {
    let g1 = new Graph();
    g1.addNode("A");
    g1.addNode("B");
    let adjList1 = g1.getAdjList();
    let adjList2 = [[],[]];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
  });

  test("Graph - two nodes, one edge; tests equality of structure not labels", () => {
    let g1 = new Graph();
    g1.addNode("A");
    g1.addNode("B");
    g1.addEdge("A","B");
    let adjList1 = g1.getAdjList();
    let adjList2 = [[1],[0]];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
  });

  test("Graph - three nodes, two edges; tests equality of structure not labels", () => {
    let g1 = new Graph();
    g1.addNode("A");
    g1.addNode("B");
    g1.addEdge("A","B");
    g1.addNode("C");
    g1.addEdge("B","C");
    let adjList1 = g1.getAdjList();
    let adjList2 = [[1],[0,2],[1]];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
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
    g1.addEdge("A","B");
    g1.addEdge("B","C");
    g1.addEdge("D","E");
    g1.addEdge("E","F");
    g1.addEdge("F","G");
    g1.addEdge("D","F");
    let adjList1 = g1.getAdjList();
    let adjList2 = [[4,6],[3,2],[1,3],[5,1,2],[0],[3],[0]];
    expect(isomorphism(adjList1,adjList2)).toBe(true);
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
  g1.addEdge("A","B");
  expect(isComplete(g1)).toBe(true);
});

test("isComplete - tests three node graphs", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("A","B");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("B","C");
  expect(isComplete(g1)).toBe(false);
  g1.addEdge("A","C");
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
  g1.addEdge()
});

test("completeGraphChecker - three node graph", () => {
  let g1 = new Graph();
  g1.addNode("A");
  g1.addNode("B");
  g1.addNode("C");
  expect(completeGraphChecker(1)(g1)).toBe(false);
  expect(completeGraphChecker(2)(g1)).toBe(false);
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("A","B");
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("B","C");
  expect(completeGraphChecker(3)(g1)).toBe(false);
  g1.addEdge("A","C");
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
  g1.addEdge("A","B");
  expect(g1.edgeCount).toBe(1);
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("B","C");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("C","D");
  expect(completeGraphChecker(4)(g1)).toBe(false);å
  g1.addEdge("A","D");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("B","D");
  expect(completeGraphChecker(4)(g1)).toBe(false);
  g1.addEdge("A","C");
  expect(g1.edgeCount).toBe(6);
  expect(completeGraphChecker(4)(g1)).toBe(true);
});