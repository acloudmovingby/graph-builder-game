package scalagraph

import scala.scalajs.js
import scala.scalajs.js.annotation._
import graphi.MapBasedSimpleGraphImmutable

// g.addNode("A")
// g.addNode("A", data = "some data")
// g.addNode[Int]("B", data = 42)
// g.addNode("C", data = List(1,2,3), label = "Node C")
// g.addEdge("A", "B")
// g.addEdge("A", "C", data = "edge data")
// g.addEdge("B", "C", data = 3.14, label = "Edge BC")
// g.addEdge("B", "C", data = 3.14, label = "Edge BC", weight = 2.0)
// if I don't want to use weights, do I care about having to enter that? Or is it stranger
// to have that or not have that depending

case class NodeData(counter: Int, x: Int, y: Int)

// This is a facade type for the JavaScript representation of NodeData, I think it has to be just raw values
// without methods, so I put the conversion methods in a singleton object
@js.native
trait NodeDataJS extends js.Object {
    val counter: Int
    val x: Int
    val y: Int
}

object NodeDataConverter {
    def toJS(data: NodeData): NodeDataJS = {
        js.Dynamic.literal(
            counter = data.counter,
            x = data.x,
            y = data.y
        ).asInstanceOf[NodeDataJS]
    }
    def toScala(js: NodeDataJS): NodeData = NodeData(js.counter, js.x, js.y)
}

@JSExportTopLevel("GraphController")
class GraphController {
    private var graph = new MapBasedSimpleGraphImmutable[Int]() // key is Int, data is NodeData
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
    def addNode(key: Int, data: NodeDataJS): Unit = {
        graph = graph.addNode(key)
        keyToData += (key -> NodeDataConverter.toScala(data))
    }
    @JSExport
    def addEdge(to: Int, from: Int): Unit = try {
        graph = graph.addEdge(to, from)
    } catch {
        case e: NoSuchElementException => println(s"Error adding edge (not yet implemented): ${e.getMessage}")
    }

    @JSExport
    def updateNodeData(key: Int, data: NodeDataJS): Unit = {
        keyToData.get(key) match {
            case Some(_) => keyToData += (key -> NodeDataConverter.toScala(data))
            case None => println(s"Error updating node data: Node $key does not exist")
        }
    }
}