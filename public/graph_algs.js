let graph1 = [[1],[0]];
let graph2 = [[]];

console.log(`edges: ${edges(graph1)}`);
/*
let map = new Map();
let result = checkMapping(graph1, graph2, map);
console.log(`checkMapping: ${result}`);
*/

function checkMapping(g1, g2, map) {
  let mapCorrect = true;
  let edges = getEdges(g1);
  edges.forEach((e) => {
    // get equivalents in g2
    // check that the first node in g2 has the other node as an edge
    let g2_source = map.get(e[0]);
    let g2_target = map.get(e[1]);
    mapCorrect += g2[g2_source].includes(g2_target);
  });
  return mapCorrect;
}

// takes two adjacency lists (arrays of arrays). This function assumes these are undirected graphs
function isomorphic(g1, g2) {
  let isomorphic = bruteForce(g1, g2);

  if (g1.length !== g2.length) {
    return false;
  }
  // first, cycle through all possible mappings
  for (let i = 0; i < g1.length; i++) {
    for (let j = 0; j < g2.length; j++) {}
  }
  return true;
}

function permute(nodes) {
  let s = "";
}

function getEdges(nodes) {
  let edges = [];
  let marked = new Set();
  for (let i = 0; i < nodes.length; i++) {
    marked.add(nodes[i]);
    for (let j = 0; j < nodes[i].length; j++) {
      if (!marked.has(nodes[i][j])) {
        edges.push([i, j]);
      }
    }
  }
  return edges;
}

function edges() {
  let edges = [];
  graph1.forEach((source,i) {
    source.forEach((target,j) {
      edges.push([i,j]);
    })
  });
  return graph1;
}