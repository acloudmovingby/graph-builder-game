const { checkEdges, isomorphism } = require("./public/graph_algs");

test("checks edges with empty graphs", () => {
  expect(checkEdges([], [], [])).toBe(true);
});

test("checks graphs with single nodes", () => {
  let g1 = [[]];
  let g2 = [[]];
  let perm = [0];
  expect(checkEdges(perm, g1, g2)).toBe(true);
});

test("checks graphs with two nodes, no edges", () => {
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

test("isomorphism - test with non-isomorphic 2/2 nodes, 1/0 edges; commutative", () => {
  let g1 = [[1], [0]];
  let g2 = [[], []];
  expect(isomorphism(g1, g2)).toBe(false);
  expect(isomorphism(g2, g1)).toBe(false);
});
