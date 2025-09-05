package scalagraph

import scala.scalajs.js.annotation._
import graphi.MapBasedSimpleGraphImmutable

case class NodeData(counter: Int, x: Int, y: Int)

@JSExportTopLevel("GraphController")
class GraphController {
    private var graph = new MapBasedSimpleGraphImmutable[Int]()
    private var keyToData = Map[Int, NodeData]()
    @JSExport
    def clearGraph(): Unit = {
        graph = new MapBasedSimpleGraphImmutable[Int]()
    }
    @JSExport
    def nodeCount(): Int = graph.nodeCount
    @JSExport
    def edgeCount(): Int = graph.edgeCount
    @JSExport
    def addNode(key: Int, counter: Int, x: Int, y: Int): Unit = {
        graph = graph.addNode(key)
        keyToData += (key -> NodeData(counter, x, y))
    }
    @JSExport
    def addEdge(to: Int, from: Int): Unit = try {
        graph = graph.addEdge(to, from)
    } catch {
        case e: NoSuchElementException => println(s"Error adding edge (not yet implemented): ${e.getMessage}")
    }

    @JSExport
    def updateNodeData(key: Int, counter: Int, x: Int, y: Int): Unit = {
        keyToData.get(key) match {
            case Some(_) => keyToData += (key -> NodeData(counter, x, y))
            case None => println(s"Error updating node data: Node $key does not exist")
        }
    }
}