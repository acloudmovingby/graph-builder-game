package scalagraph

import graphi.DirectedMapGraph

import scalagraph.{NodeData, CanvasLine}

object EdgeRender {
  // Parameters for styling the rendered shapes
  val simpleEdgeStrokeColor: String = "orange"
  val simpleEdgeStrokeWidth: Int = 8

  /**
   * Given a DirectedMapGraph and a keyToData map, returns a List of CanvasLine representing the edges.
   * Each CanvasLine contains the start and end Point, width, and color.
   *
   * @param graph The directed graph
   * @param keyToData Map from node key to NodeData (which contains x, y)
   * @param directed If true, treat as directed, else as undirected
   * @return List of CanvasLine
   */
  def getCanvasLines(
    graph: DirectedMapGraph[Int],
    keyToData: Map[Int, NodeData],
    directed: Boolean = true,
    color: String = simpleEdgeStrokeColor,
    width: Int = simpleEdgeStrokeWidth
  ): List[CanvasLine] = {
    val edges = graph.getEdges
    edges.flatMap { case (from, to) =>
      for {
        fromData <- keyToData.get(from)
        toData <- keyToData.get(to)
      } yield CanvasLine(
        from = Point(fromData.x, fromData.y),
        to = Point(toData.x, toData.y),
        width = width,
        color = color
      )
    }
  }.toList
}

