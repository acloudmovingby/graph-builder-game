import utest.*
import graphcontroller.controller.{
  ClearButtonClicked, Controller, CopyButtonClicked, ExportFormatChanged,
  MainCanvasMouseEvent, MouseEvent, RedoRequested, ToggleDirectedness,
  ToolSelected, UndoRequested
}
import graphcontroller.controller.MouseEvent
import graphcontroller.components.RenderOp
import graphcontroller.components.buildpane.BuildPaneRenderOp
import graphcontroller.components.exportpane.ExportPaneRenderData
import graphcontroller.components.exportpane.ExportFormat.Python
import graphcontroller.components.ops.SetInnerHTML
import graphcontroller.components.undobutton.UndoRedoViewData
import graphcontroller.dataobject.Vector2D
import graphcontroller.model.State
import graphcontroller.shared.BasicTool

/**
 * End-to-end happy path tests that simulate a realistic sequence of user actions,
 * using Controller.handleEventWithState so there are no DOM dependencies.
 *
 * Node layout (all positions are >36px apart to avoid accidental hover detection,
 * since baseNodeRadius = 18 and the hover check uses radius * 2 = 36):
 *
 *   node0 (200,200)          node1 (500,200)
 *
 *              node3 (350,340)        ← added later
 *
 *              node2 (350,480)
 */
object HappyPathTests extends TestSuite {

  // Canvas click positions
  private val node0      = Vector2D(200, 200)
  private val node1      = Vector2D(500, 200)
  private val node2      = Vector2D(350, 480)
  private val node3      = Vector2D(350, 340) // added later; center of the existing triangle
  private val emptySpot  = Vector2D(50,  480) // >36px from every node above

  // Bounding box for the area-complete draw (encloses all four nodes)
  private val areaTopLeft     = Vector2D(100, 100)
  private val areaTopRight    = Vector2D(600, 100)
  private val areaBottomRight = Vector2D(600, 550)
  private val areaBottomLeft  = Vector2D(100, 550)

  private def canvas(coords: Vector2D, eventType: MouseEvent) =
    MainCanvasMouseEvent(coords, eventType)

  private def step(state: State, event: graphcontroller.controller.Event): (State, Seq[RenderOp]) =
    Controller.handleEventWithState(event, state)

  /** Extracts the single ExportPaneRenderData from a renderOps list, asserting exactly one exists. */
  private def exportData(renderOps: Seq[RenderOp]): ExportPaneRenderData = {
    val matches = renderOps.collect { case d: ExportPaneRenderData => d }
    assert(matches.size == 1)
    matches.head
  }

  /** Extracts the single BuildPaneRenderOp from a renderOps list, asserting exactly one exists. */
  private def buildPaneOp(renderOps: Seq[RenderOp]): BuildPaneRenderOp = {
    val matches = renderOps.collect { case op: BuildPaneRenderOp => op }
    assert(matches.size == 1)
    matches.head
  }

  /** Extracts the single UndoRedoViewData from a renderOps list, asserting exactly one exists. */
  private def undoRedoData(renderOps: Seq[RenderOp]): UndoRedoViewData = {
    val matches = renderOps.collect { case op: UndoRedoViewData => op }
    assert(matches.size == 1)
    matches.head
  }

  /** Extracts the value of a SetInnerHTML op by element id from a BuildPaneRenderOp, asserting exactly one match. */
  private def innerHtml(bp: BuildPaneRenderOp, elementId: String): String = {
    val matches = bp.ops.collect { case SetInnerHTML(id, html) if id == elementId => html }
    assert(matches.size == 1)
    matches.head
  }

  def tests = Tests {

    test("Full user happy path") {

      // -----------------------------------------------------------------------
      // Phase 1 — Add 3 nodes by clicking the blank canvas
      // -----------------------------------------------------------------------
      val (s1, ops1) = step(State.init, canvas(node0, MouseEvent.Down))
      val (s2, _)    = step(s1,         canvas(node1, MouseEvent.Down))
      val (s3, ops3) = step(s2,         canvas(node2, MouseEvent.Down))

      assert(s3.graph.nodeCount == 3)
      assert(s3.graph.getEdges.isEmpty)

      // RenderOp check 1: after the first node is added, undo is available but redo is not
      val ur1 = undoRedoData(ops1)
      assert(ur1.canUndo)
      assert(!ur1.canRedo)

      // RenderOp check 2: build pane reflects 3 isolated nodes
      val bp3 = buildPaneOp(ops3)
      assert(innerHtml(bp3, "node-count")      == "3")
      assert(innerHtml(bp3, "edge-count")      == "0")
      assert(innerHtml(bp3, "component-count") == "3")

      // -----------------------------------------------------------------------
      // Phase 2 — Build a directed cycle: 0→1, 1→2, 2→0
      // -----------------------------------------------------------------------
      // Click node 0 to enter edge-adding mode
      val (s4, _) = step(s3, canvas(node0, MouseEvent.Down))
      assert(s4.toolState == BasicTool(Some(0)))

      val (s5, _)    = step(s4, canvas(node1, MouseEvent.Down)) // 0→1; edgeStart becomes Some(1)
      val (s6, _)    = step(s5, canvas(node2, MouseEvent.Down)) // 1→2; edgeStart becomes Some(2)
      val (s7, ops7) = step(s6, canvas(node0, MouseEvent.Down)) // 2→0; edgeStart becomes Some(0)

      assert(s7.graph.getEdges.size == 3)

      // RenderOp check 3: build pane reflects 3 edges after the directed cycle is complete
      val bp7 = buildPaneOp(ops7)
      assert(innerHtml(bp7, "node-count") == "3")
      assert(innerHtml(bp7, "edge-count") == "3")

      // Click the empty canvas to exit edge-adding mode
      val (s8, _) = step(s7, canvas(emptySpot, MouseEvent.Down))
      assert(s8.toolState == BasicTool(None))

      // -----------------------------------------------------------------------
      // Phase 3 — Switch to undirected
      // -----------------------------------------------------------------------
      val (s9, _) = step(s8, ToggleDirectedness)
      assert(!s9.isDirected)

      // -----------------------------------------------------------------------
      // Phase 4 — Undo → back to directed
      // -----------------------------------------------------------------------
      val (s10, ops10) = step(s9, UndoRequested)
      assert(s10.isDirected)
      assert(s10.graph.getEdges.size == 3) // original directed edges intact

      // RenderOp check 4: after undoing, both undo and redo are available
      val ur10 = undoRedoData(ops10)
      assert(ur10.canUndo)
      assert(ur10.canRedo)

      // -----------------------------------------------------------------------
      // Phase 5 — Redo → undirected again
      // -----------------------------------------------------------------------
      val (s11, _) = step(s10, RedoRequested)
      assert(!s11.isDirected)

      // -----------------------------------------------------------------------
      // Phase 6 — Export DOT format and copy to clipboard
      // -----------------------------------------------------------------------
      val (s12, ops12) = step(s11, CopyButtonClicked)
      assert(s12.copyToClipboard)
      val dotExport = exportData(ops12)
      assert(dotExport.clipboardContent.isDefined)
      val dotContent = dotExport.clipboardContent.get
      assert(dotContent.startsWith("graph"))   // undirected DOT header
      assert(dotContent.contains("--"))        // undirected edge syntax

      // -----------------------------------------------------------------------
      // Phase 7 — Switch to Python format and copy
      // -----------------------------------------------------------------------
      val (s13, _)     = step(s12, ExportFormatChanged(Python))
      assert(s13.exportFormat == Python)
      val (s14, ops14) = step(s13, CopyButtonClicked)
      assert(s14.copyToClipboard)
      val pyExport = exportData(ops14)
      assert(pyExport.clipboardContent.isDefined)
      val pyContent = pyExport.clipboardContent.get
      assert(pyContent.contains("0"))
      assert(pyContent.contains("1"))
      assert(pyContent.contains("2"))

      // -----------------------------------------------------------------------
      // Phase 8 — Clear the graph
      // -----------------------------------------------------------------------
      val (s15, ops15) = step(s14, ClearButtonClicked)
      assert(s15.graph.nodeCount == 0)
      assert(s15.graph.getEdges.isEmpty)

      // RenderOp check 5: build pane zeroes out after clear
      val bp15 = buildPaneOp(ops15)
      assert(innerHtml(bp15, "node-count") == "0")
      assert(innerHtml(bp15, "edge-count") == "0")

      // -----------------------------------------------------------------------
      // Phase 9 — Undo the clear; graph comes back
      // -----------------------------------------------------------------------
      val (s16, _) = step(s15, UndoRequested)
      assert(s16.graph.nodeCount == 3)
      assert(!s16.isDirected) // was undirected before the clear

      // -----------------------------------------------------------------------
      // Phase 10 — Add a 4th node
      // -----------------------------------------------------------------------
      val (s17, _) = step(s16, canvas(node3, MouseEvent.Down))
      assert(s17.graph.nodeCount == 4)

      // -----------------------------------------------------------------------
      // Phase 11 — Area complete: connect all 4 nodes by tracing a bounding box
      //
      // ToolSelected → MouseEvent.Down (start draw)
      //              → MouseEvent.Move × 3 (trace rectangle)
      //              → MouseEvent.Up   (complete; isInside ray-cast picks up all 4 nodes)
      // -----------------------------------------------------------------------
      val (s18, _) = step(s17, ToolSelected("area-complete"))
      val (s19, _) = step(s18, canvas(areaTopLeft,     MouseEvent.Down))
      val (s20, _) = step(s19, canvas(areaTopRight,    MouseEvent.Move))
      val (s21, _) = step(s20, canvas(areaBottomRight, MouseEvent.Move))
      val (s22, _) = step(s21, canvas(areaBottomLeft,  MouseEvent.Move))
      val (s23, ops23) = step(s22, canvas(areaTopLeft, MouseEvent.Up))

      assert(s23.graph.nodeCount == 4)
      // K4 complete undirected graph has 6 edges
      assert(s23.graph.edgeCount >= 6)

      // RenderOp check 6: build pane reflects the K4 result
      val bp23 = buildPaneOp(ops23)
      assert(innerHtml(bp23, "node-count").toInt == 4)
      assert(innerHtml(bp23, "edge-count").toInt >= 6)

      // -----------------------------------------------------------------------
      // Phase 12 — Switch back to directed
      // -----------------------------------------------------------------------
      val (s24, _) = step(s23, ToggleDirectedness)
      assert(s24.isDirected)
      assert(s24.graph.nodeCount == 4)
    }

  }
}
