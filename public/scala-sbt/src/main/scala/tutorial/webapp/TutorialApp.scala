package tutorial.webapp

import scala.scalajs.js.annotation._

import graphi.Test
import graphi.MapBasedSimpleGraphImmutable

@JSExportTopLevel("TutorialApp")
object TutorialApp {
  def main(args: Array[String]): Unit = {
    println(s"Hello world? ${new Test().saySomething()}")
  }
  @JSExport
  def foo(): String = "bar"
}

@JSExportTopLevel("TestWrapper")
object TestWrapper {
    @JSExport
    def createTest(): Test = new Test()
}

@JSExportTopLevel("GraphController")
object GraphController {
    private var graph = new MapBasedSimpleGraphImmutable[String]()
    @JSExport
    def clearGraph(): Unit = {
        graph = new MapBasedSimpleGraphImmutable[String]()
    }
    @JSExport
    def nodeCount(): Int = graph.nodeCount
    @JSExport
    def edgeCount(): Int = graph.edgeCount
    def add
}

// TODO: convert this Javascript code to Scala.js
/*
let graph = new Digraph();
function clearGraph() {
  addToUndo(undoGraphStates, graph);
  graph = new Graph();
  exitBasicEdgeMode();
  exitMagicEdgeMode();
  toolState.curTool = basicTool;
  nodeHover = null;
  basicTool.state.stillInNode = false;
  refreshHtml(graph, toolState);
}
 */



