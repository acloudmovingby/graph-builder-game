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

exports.checkEdges = checkEdges;
exports.isomorphism = isomorphism;
