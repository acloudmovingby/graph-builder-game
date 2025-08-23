export class Graph {
    constructor() {
        this.adjList = [];
        this.nodeCount = 0;
        this.edgeCount = 0;
        this.nodeValues = new Map(); // maps the values stored in the nodes (the "labels") to their indices in the adjacency list.
        this.indices = new Map(); // maps indices to the values stored in nodes (the "labels")
    }

    // Don't do anything computationally intensive here because it seems this gets used O(n) times per adding node in the UI (where n = nodeCount)
    // This addNode function is used often to build up graphs for comparisons, tests, etc., so it should be lightweight
    // TODO: I'm noticing this is not at all thread-safe, not sure how much that'll matter for current use cases
    addNode(nodeValue) {
        this.adjList.push([]);
        // TODO: This is not actually checking if the node already exists in the graph; this would definitely cause problems
        this.nodeValues.set(nodeValue, this.adjList.length - 1);
        this.indices.set(this.adjList.length - 1, nodeValue);
        this.nodeCount++;
    }

    // returns true only if the graph contains these nodes already and the edge didn't already exist;
    // does NOT allow parallel edges (multiple edges between two nodes)
    // does NOT allow self edges (node connected to itself)

    // WARNING TODO: this function assumes bidirectional graph. When I add directed graphs, they should either be a different class or at least use a different method for insertion
    addEdge(nodeValue1, nodeValue2) {
        console.assert(nodeValue1 !== nodeValue2, "addEdge hasn't been designed yet to be used for self-edges");
        if (nodeValue1 === nodeValue2) {
            return false;
        }

        let containsNodes =
            this.nodeValues.has(nodeValue1) && this.nodeValues.has(nodeValue2);
        let addedEdge = false;
        if (containsNodes) {
            let index1 = this.nodeValues.get(nodeValue1);
            let index2 = this.nodeValues.get(nodeValue2);
            let oneTwoExists = this.adjList[index1].includes(index2);
            let twoOneExists = this.adjList[index2].includes(index1);

            if (!oneTwoExists || !twoOneExists) {
                console.assert(
                    !oneTwoExists && !twoOneExists,
                    "graph had a directed edge; addEdge assumes graph is bidirectional; edge added anyway"
                );
                addedEdge = true;
                this.adjList[index1].push(index2);
                this.adjList[index2].push(index1);
                this.edgeCount++;
            }
        }
        return addedEdge;
    }

    // sees if graph has directed edge from 1 to 2 (but not 2 to 1)
    containsEdge(nodeValue1, nodeValue2) {
        if (!this.nodeValues.has(nodeValue1) || !this.nodeValues.has(nodeValue2)) {
            return false;
        } else {
            let index1 = this.nodeValues.get(nodeValue1);
            let index2 = this.nodeValues.get(nodeValue2);
            let adjList = this.getAdjList();
            return adjList[index1].includes(index2) && adjList[index2].includes(index1);
        }
    }

    // returns adjacency list as just indices (the pure structure of the graph without the values it stores)
    getAdjList() {
        return this.adjList;
    }

    // returns iterator
    getNodeValues() {
        return this.nodeValues.keys();
    }

    // returns a list of bidirectional edges
    // each pair of vertices only appears once; in other words, it  returns either (A,B) or (B,A) but not both
    // vertex order for each edge unspecified
    // actually references node values, so be careful mutating
    // assumes graph is entirely bidirectional
    getEdges() {
        let edges = this.getEdgeIndices();
        return edges.map((e) => [this.indices.get(e[0]), this.indices.get(e[1])]);
    }

    // tuple of ints
    getEdgeIndices() {
        let marked = Array.from({
            length: this.nodeCount
        }).map((x) => false);
        let edges = [];
        for (let i = 0; i < this.adjList.length; i++) {
            marked[i] = true;
            for (let j = 0; j < this.adjList[i].length; j++) {
                let targetIndex = this.adjList[i][j];
                if (!marked[targetIndex]) {
                    edges.push([i, this.adjList[i][j]]);
                }
            }
        }
        return edges;
    }

    // throws error if nodeValue doesn't exist in graph
    getNeighbors(nodeValue) {
        let index = this.nodeValues.get(nodeValue);
        return this.getAdjList()[index].map((node) => this.indices.get(node));
    }

    // clones the graph
    // Needs nodeCopyFunction because Graph is unaware of the contents it is storing, so it cannot perform a deep copy on them without being given a function to do so.
    // This has a nice side effect of making it easy to "map" a graph. You can make nodeCopyFunction any kind of function you want, the graph structure won't be affected.
    clone(nodeCopyFunction) {
        let clone = new Graph();
        let nodeMap = new Map(); // maps this graph's node values to the clone's new node values
        for (const node of this.getNodeValues()) {
            let nodeClone = nodeCopyFunction(node);
            clone.addNode(nodeClone);
            nodeMap.set(node, nodeClone);
        }

        for (const edge of this.getEdges()) {
            let start = nodeMap.get(edge[0]);
            let end = nodeMap.get(edge[1]);
            clone.addEdge(start, end);
        }
        console.assert(
            this.nodeCount === clone.nodeCount,
            `Node counts don't match`
        );
        console.assert(
            this.edgeCount === clone.edgeCount,
            `Edge counts don't match`
        );
        return clone;
    }

}

export class Digraph extends Graph {
    constructor() {
        super();
    }

    // returns true only if the graph contains these nodes already and the edge didn't already exist;
    // does NOT allow parallel edges (multiple edges between two nodes)
    // does NOT allow self edges (node connected to itself)

    // WARNING TODO: this function assumes bidirectional graph. When I add directed graphs, they should either be a different class or at least use a different method for insertion
    addEdge(nodeValue1, nodeValue2) {
        console.assert(nodeValue1 !== nodeValue2, "addEdge hasn't been designed yet to be used for self-edges");
        if (nodeValue1 === nodeValue2) {
            return false;
        }

        let containsNodes =
            this.nodeValues.has(nodeValue1) && this.nodeValues.has(nodeValue2);
        let addedEdge = false;
        if (containsNodes) {
            let index1 = this.nodeValues.get(nodeValue1);
            let index2 = this.nodeValues.get(nodeValue2);
            let oneTwoExists = this.adjList[index1].includes(index2);

            addedEdge = true;
            if (!oneTwoExists) {
                this.adjList[index1].push(index2);
                this.edgeCount++;
            }
        }
        return addedEdge;
    }

    // sees if graph has directed edge from 1 to 2 (but not 2 to 1)
    containsEdge(nodeValue1, nodeValue2) {
        if (!this.nodeValues.has(nodeValue1) || !this.nodeValues.has(nodeValue2)) {
            return false;
        } else {
            let index1 = this.nodeValues.get(nodeValue1);
            let index2 = this.nodeValues.get(nodeValue2);
            let adjList = this.getAdjList();
            return adjList[index1].includes(index2);
        }
    }

    // returns a list of directed edges
    // vertex order for each edge is (from, to)
    // actually references node values, so be careful mutating
    getEdges() {
        let edges = [];
        for (let i = 0; i < this.adjList.length; i++) {
            for (let j = 0; j < this.adjList[i].length; j++) {
                edges.push([this.indices.get(i), this.indices.get(this.adjList[i][j])]);
            }
        }
        return edges;
    }
}