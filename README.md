# graph-builder-game

The original "game" with Easter Eggs: http://tranquil-oasis-04623.herokuapp.com/
The tooling I'm currently developing: http://tranquil-oasis-04623.herokuapp.com/tools

FEEDBACK WELCOME!!

In computer science, networks (called graphs in CS) are used in all kinds of applications. I wanted to make a WYSIWYG (what you see is what you get) tool to build graphs: 
# GOALS:
* A WYSIWYG tool to build graphs quickly
* provide a way to export in a variety of formats to be implemented in code or other applications
* Make it feel fun and intuitive
# WHY?
* In an earlier project I was using graphs to write custom pathfinding algorithms for map data. However, building test cases was not easy and for some situations I needed graphs of 20 or 30 nodes. Writing these test cases out on paper was tedious and error prone
* I want to make it easier to quickly build small to medium size graphs.
# NOT-GOALS ("you have to say 'no' to 90% of feature ideas"):
* Graph analysis - there are many, many tools available out there that take graph data and can analyze the heck out of it. I'm not trying to reinvent all of graph theory here. The goal with this app is to help you construct a graph visually and then you can export it to be analyzed or used however you want later
* Creating pretty pictures - Graph drawing is a fascinating problem, and in the future I might try to have more ways to move or customize the appearance of the graph, but there are tools out there that do this well: (1) there are [tools](https://neo4j.com/developer/tools-graph-visualization/) that, given the raw graph data, can visualize the graph. The issue is that they are fiddly and not WYSIWYG (2) Actual art tools like Figma/Illustrator/many others that help you draw prety diagrams or other vector based art. I don't have time to reinvent all of Adobe Illustrator :) 

#What Tools to Add?
I've been trying lots of different tool types (see below), and these are my findings:
* What tool is best really depends on what kind of graph you're building (sparsely connected, nearly complete, a tree, etc.)
* Fewer, less "efficient" tools are better than too many tools: feature bloat is dangerous, not just because users get confused but it also multiplies all the interactions and tech debt you have to deal with.

#Tool Examples
I gave a little bounce to the nodes, but the real goal was to be able to make nodes with a single click--and pepper the screen with them quickly if I wanted to.

![Basic use tool](screenshots/basic-use-4.gif)

I quickly realized that when building graphs, myself and others who used the program often found it more intutitive to build the edges in sequentially as a path, rather than repeatedly adding edges radiating out from a point. Of course, which method is faster depends on the structure of the graph.

I really wanted to see what felt fun. This tool I experimented with allowed me to quickly select a whole range of nodes and connect them all at once! Very satisfying.

![Complete tool](screenshots/drag-complete-tool-1.gif)

This tool also was interesting because it builds edges without clicking. You simply move the target area over nodes and it automatically adds edges.

![Path tool](screenshots/path-tool-1.gif)

# Briefly, what are graphs?
Graphs in computer science were poorly named as they're easily confused with the graphs we learn about in high school. Graphs in CS, however, are really about networks. 

Graphs are represented as a collection of "nodes" connected together. These nodes can also be called vertices, and the relationship between two nodes is called an edge. A simple example might be a family tree, where each parent node connects to their children nodes. In that example, the people are the nodes and edges represent their familial relationships. 

Technologies ranging from Google maps to protein identification to fingerprint identification are all made possibly by graphs. Graphs are fascinating becasue they can be used to model practically any situation because they essentially are about a collection of entities and the relationships between those entities.

For a more in-depth reading, checkout [wikipedia](https://en.wikipedia.org/wiki/Graph_theory) or [this nice introduction](https://medium.com/basecs/a-gentle-introduction-to-graph-theory-77969829ead8). 
