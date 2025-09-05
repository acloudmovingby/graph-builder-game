package scalagraph

import scala.scalajs.js.annotation._
import graphi.MapBasedSimpleGraphImmutable

case class NodeData(counter: Int, x: Int, y: Int)

@JSExportTopLevel("GraphController")
class GraphController {
    private var graph = new MapBasedSimpleGraphImmutable[NodeData]()
    @JSExport
    def clearGraph(): Unit = {
        graph = new MapBasedSimpleGraphImmutable[NodeData]()
    }
    @JSExport
    def nodeCount(): Int = graph.nodeCount
    @JSExport
    def edgeCount(): Int = graph.edgeCount
    @JSExport
    def addNode(counter: Int, x: Int, y: Int): Unit = {
        graph = graph.addNode(NodeData(counter, x, y))
    }
    @JSExport
    def addEdge(counter1: Int, x1: Int, y1: Int, counter2: Int, x2: Int, y2: Int): Unit = try {
        val node1 = NodeData(counter1, x1, y1)
        val node2 = NodeData(counter2, x2, y2)
        graph = graph.addEdge(node1, node2)
    } catch {
        case e: NoSuchElementException => println(s"Error adding edge (not yet implemented): ${e.getMessage}")
    }
}