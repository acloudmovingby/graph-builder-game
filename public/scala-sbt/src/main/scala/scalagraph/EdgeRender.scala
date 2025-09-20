package scalagraph

import scala.math
import graphi.DirectedMapGraph

import scalagraph.{CanvasLine, NodeData, Point}

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

  // Scala version of decideDirectionality from edge_render.mjs
  case class DirectedEdge(bidirectional: Boolean, from: Point, to: Point)

  /**
   * Given a list of edges (as tuples of Points), returns a list of DirectedEdge objects.
   * If both (A,B) and (B,A) exist, the edge is bidirectional.
   * Each edge is only included once (either direction).
   */
  def decideDirectionality(edges: List[(Point, Point)]): List[DirectedEdge] = {
    val edgeSet = edges.toSet
    var seen = Set.empty[(Point, Point)]
    val directedEdges = scala.collection.mutable.ListBuffer[DirectedEdge]()
    for ((from, to) <- edges) {
      val key = (from, to)
      val reverseKey = (to, from)
      if (!seen.contains(key) && !seen.contains(reverseKey)) {
        val bidirectional = edgeSet.contains(reverseKey)
        directedEdges += DirectedEdge(bidirectional, from, to)
        seen += key
        seen += reverseKey
      }
    }
    directedEdges.toList
  }

  /**
   * Trims the start or end of each edge by a given displacement, moving the start or end Point closer to the other.
   * @param trimStart If true, trims the start of the edge; if false, trims the end.
   * @param edges List of (Point, Point) representing edges.
   * @param displacement How far to trim from the node (default: 47, matching JS logic for arrowDisplacement)
   * @return List of (Point, Point) with trimmed endpoints.
   */
  def trimEdges(
    trimStart: Boolean,
    edges: List[(Point, Point)],
    displacement: Double = 47.0 // JS: nodeRadius + (triangleHeight * scale_factor) + arrowPadding
  ): List[(Point, Point)] = {
    edges.map { case (from, to) =>
      val dx = to.x - from.x
      val dy = to.y - from.y
      val edgeLength = math.sqrt(dx * dx + dy * dy)
      if (edgeLength == 0) (from, to)
      else {
        val ratio = (displacement - 1) / edgeLength
        val dxFromNode = (dx * ratio).toInt
        val dyFromNode = (dy * ratio).toInt
        if (trimStart) {
          (Point(from.x + dxFromNode, from.y + dyFromNode), to)
        } else {
          (from, Point(to.x - dxFromNode, to.y - dyFromNode))
        }
      }
    }
  }

  /**
   * Trims edges based on directionality: if bidirectional, trims both ends; otherwise, trims only the end.
   * Mirrors the JS trimEdgesBasedOnDirectionality.
   * @param directedEdges List of DirectedEdge
   * @param displacement How far to trim from the node (default: 47.0)
   * @return List of (Point, Point) with trimmed endpoints
   */
  def trimEdgesBasedOnDirectionality(
    directedEdges: List[DirectedEdge],
    displacement: Double = 47.0
  ): List[(Point, Point)] = {
    directedEdges.map { de =>
      if (de.bidirectional) {
        // trim both ends
        val frontTrimmed = trimEdges(trimStart = true, List((de.from, de.to)), displacement).head
        trimEdges(trimStart = false, List(frontTrimmed), displacement).head
      } else {
        // trim only end
        trimEdges(trimStart = false, List((de.from, de.to)), displacement).head
      }
    }
  }
}
