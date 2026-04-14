# SelectTool Implementation Plan — Part 2 (Steps 5–12)

**Parent design docs:** [select-tool.md](select-tool.md), [select-tool-implementation.md](select-tool-implementation.md)  
**Date:** 2026-04-13  
**Prerequisite:** Steps 1–4 are already implemented. The codebase currently has:
- Keyboard shortcuts: `V` → SelectTool, `B` → BasicTool, `Esc` → clear selection or exit tool
- `SelectMode` enum with `Idle` and `DraggingBox(startPoint)` in `Tool.scala`
- `selectedNodes: Set[Int]` on `State` (not in undo stack)
- `nodesInRect(corner1, corner2)` helper on `State`
- `Selected` style in `NodeRenderStyle` (dashed ring around node)
- `nodesWithStyles` accepts `selectedNodes` parameter and overrides styles for selected nodes
- `handleSelectTool` in `MainCanvasComponent` computes `selectedNodes` on mouse-up from `DraggingBox`
- `EscapePressed` event with two-stage behavior (clear selection first, then exit tool)
- Selection cleared when switching away from SelectTool

## Note on MoveTool

MoveTool is **kept for now** — do not remove or deprecate it. Once Steps 10 and 11 are complete (BasicTool drag-to-move and multi-node drag in SelectTool), MoveTool will become redundant and can be deprecated in a future pass. Add a `// TODO: MoveTool can be deprecated once BasicTool drag-to-move (Step 10) and multi-node drag (Step 11) are complete` comment to `MoveTool` in `Tool.scala`.

---

## Implementation Order

| Step | What | Complexity |
|------|------|-----------|
| 5 | Live selection preview while dragging | Small |
| 6 | Bounding box around selected nodes | Small–Medium |
| 7 | Delete selected nodes (placeholder) | Small |
| 8 | Shift+click to toggle selection | Medium |
| 9 | Double-click to add node in SelectTool | Medium |
| 10 | BasicTool drag-to-move | Medium–Hard |
| 11 | Multi-node drag in SelectTool | Medium |
| 12 | Info pane scoped to selection | Medium |

---

## Step 5: Live Selection Preview While Dragging

**Goal:** As the user drags a selection rectangle, nodes inside the rectangle should immediately show the `Selected` dashed ring — before the mouse is released. This gives real-time visual feedback of what will be selected.

### How it works

Currently, `handleSelectTool` does nothing on `Move` events while in `DraggingBox`:
```scala
case Move => state // view reads lastMousePosition for the live box corner
```

The fix: on every `Move` while in `DraggingBox`, compute which nodes fall inside the current rectangle and update `selectedNodes`. The view already renders `Selected` style for nodes in `selectedNodes`, so the dashed rings will appear automatically.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

In `handleSelectTool`, update the `Move` case:

```scala
case Move =>
    tool.mode match {
        case DraggingBox(startPoint) =>
            val potentialSelection = state.nodesInRect(startPoint, event.coords)
            state.copy(selectedNodes = potentialSelection)
        case _ => state
    }
```

That's it. The view code (`nodesWithStyles`) already handles rendering `Selected` style for any node in `selectedNodes`. The selection rectangle is already drawn via `selectionBox` using `lastMousePosition`.

### Performance note

`nodesInRect` iterates over all nodes on every mouse move. For typical graph sizes (tens to low hundreds of nodes) this is fine. If performance ever matters, we can optimise later.

### Tests

**In `MainCanvasComponentTests.scala`:**

- "SelectTool - live preview during drag": Set up 3 nodes, start a DraggingBox at (0,0), fire a Move event to (50,50). Assert that nodes inside (0,0)→(50,50) are in `selectedNodes`. Fire another Move to (25,25) — assert the set shrinks. This tests that `selectedNodes` updates on every move, not just on release.
- "SelectTool - live preview clears when box shrinks past nodes": Ensure nodes that were in the box but are no longer (because the user moved the mouse back) are removed from `selectedNodes`.

---

## Step 6: Bounding Box Around Selected Nodes

**Goal:** After selection, draw a rectangle around all selected nodes (a "bounding box" that hugs the selected nodes). This is separate from the selection drag rectangle — it persists after the mouse is released and visually groups the selection. Figma does this.

### How it works

The bounding box is computed from the positions of all selected nodes — find the min/max x and y, add some padding, and draw a dashed rectangle. This is purely a view concern.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasView.scala`

Add a new function `selectionBoundingBox`:

```scala
def selectionBoundingBox(selectedNodes: Set[Int], keyToData: Map[Int, NodeData]): Seq[CanvasRenderOp] = {
    if (selectedNodes.size < 2) return Seq.empty // no bounding box for 0 or 1 nodes (the dashed ring is enough for 1)

    val selectedData = selectedNodes.flatMap(keyToData.get)
    if (selectedData.isEmpty) return Seq.empty

    val padding = NodeRender.baseNodeRadius + 10 // some breathing room around nodes
    val minX = selectedData.map(_.x).min - padding
    val minY = selectedData.map(_.y).min - padding
    val maxX = selectedData.map(_.x).max + padding
    val maxY = selectedData.map(_.y).max + padding

    val rect = Rectangle(Vector2D(minX, minY), maxX - minX, maxY - minY)
    Seq(RectangleCanvas(
        rect,
        fillColor = "rgba(0, 0, 0, 0)", // transparent fill
        borderColor = Some(NodeRender.color1),
        borderWidth = Some(1.5),
        lineDashSegments = Seq(6, 4)
    ))
}
```

**Check:** `RectangleCanvas` may or may not support `borderWidth` and `lineDashSegments`. Look at the case class definition in `public/scalajs/graphcontroller/src/graphcontroller/dataobject/canvas/RectangleCanvas.scala` first. If it only supports `borderColor` (not width/dash), you may need to add those fields. Follow the pattern from `CircleCanvas` which already supports `lineDashSegments` and `borderWidth`.

**Change the `render` method** in `MainCanvasView` to include the bounding box. Add `selectionBoundingBox(...)` to the shapes sequence in `MainCanvasView.render()`. It should be drawn *behind* the nodes (so add it before `nodes(state)` in the concatenation):

```scala
def render(state: State): MainCanvasViewData = {
    MainCanvasViewData(
        edges(state)
            ++ potentialEdges(state)
            ++ edgeAddingIndicatorLine(state)
            ++ selectionBoundingBox(state.selectedNodes, state.keyToData) // <-- new, before nodes
            ++ nodes(state)
            ++ areaComplete(state)
            ++ selectionBox(state.toolState, state.canvasInteraction.lastMousePosition),
        ...
    )
}
```

### Tests

- "Bounding box is empty when fewer than 2 nodes selected": 0 or 1 selected → `Seq.empty`.
- "Bounding box encloses all selected nodes": 3 nodes at (10,10), (50,50), (30,70). Check the returned `RectangleCanvas` has coordinates that enclose all three with padding.
- "Bounding box ignores unselected nodes": 3 nodes exist but only 2 are selected — the bounding box should only fit the 2.

---

## Step 7: Delete Selected Nodes (Placeholder)

**Goal:** Pressing `Delete` or `Backspace` when nodes are selected prints a message to the console. The `graphi` library does not yet support node removal, so this is a placeholder.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/controller/Event.scala`

Add:
```scala
case object DeleteSelectedNodes extends Event
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/toolbar/eventlisteners/KeyboardShortcutListeners.scala`

Add `Delete` and `Backspace` to the key match inside the existing `keydown` handler:
```scala
case "Delete" | "Backspace" => dispatch(DeleteSelectedNodes)
```

This goes inside the same guard (not in an input field, no modifier keys) as the existing `V`/`B`/`Esc` handling.

**New component or add to existing:** Create a small component to handle this event, or add it to an existing component. The simplest approach: add a case in `MainCanvasComponent.update` (since it already handles `MainCanvasMouseEvent` and SelectTool logic).

In `MainCanvasComponent.update`:
```scala
override def update(state: State, event: Event): State = {
    event match {
        case m: MainCanvasMouseEvent => mouseMoveHandling(state, m)
        case DeleteSelectedNodes if state.selectedNodes.nonEmpty =>
            println(s"Deleting nodes: ${state.selectedNodes.toSeq.sorted.mkString(", ")}")
            // TODO: Actually remove nodes from graph once graphi library supports node removal
            state.copy(selectedNodes = Set.empty)
        case _ => state
    }
}
```

Note: we clear `selectedNodes` after "deleting" so the UI resets, even though the nodes aren't actually removed from the graph yet.

**Import** `DeleteSelectedNodes` in `MainCanvasComponent.scala`.

### Tests

**In `MainCanvasComponentTests.scala`:**
- "DeleteSelectedNodes clears selectedNodes": Create state with selected nodes, fire `DeleteSelectedNodes`, assert `selectedNodes` is empty.
- "DeleteSelectedNodes with empty selection is a no-op": Assert state is unchanged.

---

## Step 8: Shift+Click to Toggle Selection

**Goal:** Holding Shift while clicking a node adds it to or removes it from the current selection, without clearing the rest.

### How it works

This requires knowing whether Shift is held during a mouse event. Currently `MainCanvasMouseEvent` only has `coords` and `eventType` — it does not carry modifier key state. We need to add it.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/controller/Event.scala`

Add a `shiftKey` field to `MainCanvasMouseEvent`:
```scala
case class MainCanvasMouseEvent(
    coords: Vector2D,
    eventType: MouseEvent,
    shiftKey: Boolean = false
) extends Event
```

The default `false` means existing code and tests don't need to change.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/eventlisteners/MainCanvasEventListeners.scala`

Pass `e.shiftKey` through in all four event methods (`mouseMove`, `mouseDown`, `mouseUp`, `mouseLeave`):
```scala
def mouseDown(e: dom.MouseEvent): Event = {
    val coords = relativeCoordinates(e)
    MainCanvasMouseEvent(Vector2D(coords._1, coords._2), Down, shiftKey = e.shiftKey)
}
```
Do the same for `mouseMove`, `mouseUp`, `mouseLeave`.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

In `handleSelectTool`, update the `Down` + `Some(node)` case:
```scala
case Down =>
    maybeHoveredNode match {
        case Some(node) =>
            if (event.shiftKey) {
                // Toggle: add if not selected, remove if already selected
                val newSelection = if (state.selectedNodes.contains(node))
                    state.selectedNodes - node
                else
                    state.selectedNodes + node
                state.copy(toolState = SelectTool(Idle), selectedNodes = newSelection)
            } else {
                // Normal click: select just this node (clear others)
                state.copy(toolState = SelectTool(Idle), selectedNodes = Set(node))
            }
        case None =>
            // Clicking empty canvas: clear selection and start box
            state.copy(toolState = SelectTool(DraggingBox(event.coords)), selectedNodes = Set.empty)
    }
```

### Tests

**In `MainCanvasComponentTests.scala`:**
- "SelectTool - shift+click adds node to selection": Start with node 0 selected. Shift+click node 1 → `selectedNodes == Set(0, 1)`.
- "SelectTool - shift+click removes node from selection": Start with nodes 0 and 1 selected. Shift+click node 1 → `selectedNodes == Set(0)`.
- "SelectTool - click without shift still replaces selection": Start with node 0 selected. Click node 1 (no shift) → `selectedNodes == Set(1)`.

Test construction example:
```scala
test("SelectTool - shift+click adds to selection") {
    val stateWithSelection = initState
        .addNode(Vector2D(100, 100)) // 0
        .addNode(Vector2D(200, 200)) // 1
        .copy(toolState = SelectTool(), selectedNodes = Set(0))

    val shiftClickEvent = MainCanvasMouseEvent(Vector2D(200, 200), MouseEvent.Down, shiftKey = true)
    val newState = MainCanvasComponent.update(stateWithSelection, shiftClickEvent)
    assert(newState.selectedNodes == Set(0, 1))
}
```

---

## Step 9: Double-Click to Add Node in SelectTool

**Goal:** Double-clicking on the empty canvas while in SelectTool adds a new node at the click position, without switching tools.

### How it works

The browser fires a native `dblclick` event. We add a listener for it and dispatch a new event. The component handles it by adding a node (same as BasicTool does) but staying in SelectTool.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/controller/Event.scala`

Add:
```scala
case class CanvasDoubleClick(coords: Vector2D) extends Event
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/eventlisteners/MainCanvasEventListeners.scala`

The current `MainCanvasEventListeners` extends `CanvasEventListeners`. Check how the base trait works — it likely auto-registers `mousedown`, `mousemove`, `mouseup`, `mouseleave` handlers. You'll need to add a `dblclick` handler.

Look at the `CanvasEventListeners` trait to understand the registration pattern. If it provides an `init` method that auto-registers the four mouse events, you may need to override `init` or add the `dblclick` registration alongside. The simplest approach:

If `CanvasEventListeners` has a method you can override for `dblclick`, do that. Otherwise, add a manual `addEventListener("dblclick", ...)` in the `init` method of `MainCanvasEventListeners`. The handler should:
1. Get relative coordinates (same as other mouse handlers)
2. Dispatch `CanvasDoubleClick(Vector2D(x, y))`

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

In `update`, add a case for `CanvasDoubleClick`:
```scala
case CanvasDoubleClick(coords) =>
    state.toolState match {
        case _: SelectTool =>
            val maybeNode = hoveredNode(coords, state.keyToData)
            maybeNode match {
                case None =>
                    // Double-click on empty space: add a node, stay in SelectTool
                    state.addNode(coords)
                case Some(_) =>
                    // Double-click on existing node: no-op for now
                    // TODO: future — could open label editing
                    state
            }
        case _ => state // double-click does nothing in other tools
    }
```

**Import** `CanvasDoubleClick` in `MainCanvasComponent.scala`.

### Browser interaction note

When the user double-clicks, the browser fires: `mousedown`, `mouseup`, `click`, `mousedown`, `mouseup`, `click`, `dblclick`. This means the first `mousedown` will already fire a `MainCanvasMouseEvent(Down)` which, on empty canvas in SelectTool, starts a DraggingBox and clears selection. The second `mousedown` does the same. Then the `dblclick` fires and adds the node.

This should work fine because:
- The two mousedowns start/reset DraggingBox (no harm done — the box is tiny/zero-size)
- The mouseups resolve the DraggingBox with an empty selection (the box is too small to contain anything)
- The dblclick then adds the node

However, if the node count check/timing feels janky, consider adding a small dead-zone to DraggingBox selection (e.g., don't select if the rectangle is smaller than 5x5 pixels). This is optional polish.

### Tests

**In `MainCanvasComponentTests.scala`:**
- "SelectTool - double-click on empty canvas adds node": State in SelectTool, fire `CanvasDoubleClick(Vector2D(100, 100))`, assert `graph.nodeCount` increased by 1 and the new node is at (100, 100).
- "SelectTool - double-click on existing node is a no-op": Node at (100, 100), fire `CanvasDoubleClick(Vector2D(100, 100))`, assert graph unchanged.
- "Double-click in BasicTool is a no-op": Assert no state change.

---

## Step 10: BasicTool Drag-to-Move

**Goal:** In BasicTool, clicking and dragging an existing node should move it, rather than immediately entering edge-adding mode. Releasing without significant movement should enter edge-adding mode as before. This is the most user-requested change (from user interviews).

### The challenge

Currently, `handleBasicTool` reacts on `Down`:
- Click on node → immediately enters edge-adding mode (`BasicTool(Some(hoveredNode))`)

We need to **defer** that decision until we know whether the user is clicking or dragging.

### Approach: add a `PendingAction` state to BasicTool

Replace `BasicTool(edgeStart: Option[Int])` with a richer state:

```scala
case class BasicTool(mode: BasicToolMode = BasicToolMode.Idle) extends Tool { ... }

enum BasicToolMode {
    case Idle                                          // no edge adding, no pending action
    case PendingClickOrDrag(node: Int, startCoords: Vector2D) // mouse is down on a node; waiting to see if it's a click or drag
    case Dragging(node: Int)                           // confirmed drag — moving the node
    case AddingEdge(startNode: Int)                    // in edge-adding mode (was: BasicTool(Some(startNode)))
}
```

### Behaviour

| Event | Current mode | Action |
|-------|-------------|--------|
| Down on empty space | Any (except AddingEdge) | Add node (existing behaviour) |
| Down on empty space | AddingEdge | Exit edge-adding mode (existing behaviour) |
| Down on node | Idle | Enter `PendingClickOrDrag(node, coords)` |
| Down on node | AddingEdge(start) | If same node: exit edge mode. If different: add edge (existing behaviour) |
| Move | PendingClickOrDrag(node, start) | If distance from start > threshold (e.g. 5px): switch to `Dragging(node)` and push undo state. If under threshold: stay in PendingClickOrDrag. |
| Move | Dragging(node) | Update node position (same as current MoveTool logic) |
| Up | PendingClickOrDrag(node, _) | User clicked without dragging → enter `AddingEdge(node)` |
| Up | Dragging(_) | Finish drag → return to `Idle` |
| Leave | Any | Reset to `Idle` |

The **drag threshold** (5px) prevents tiny accidental mouse movements from being interpreted as drags.

### Files to change

This is a larger refactor because `BasicTool(edgeStart: Option[Int])` is referenced throughout the codebase.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/shared/Tool.scala`

Add `BasicToolMode` enum (like we did with `SelectMode`). Update `BasicTool` to use it.

**Important:** `BasicTool(None)` is used in many places to mean "idle, no edge adding." After the refactor, this becomes `BasicTool()` or `BasicTool(BasicToolMode.Idle)`. `BasicTool(Some(n))` becomes `BasicTool(BasicToolMode.AddingEdge(n))`.

**Grep the codebase** for `BasicTool(` — there are many references in:
- `MainCanvasComponent.scala` (the handler itself)
- `MainCanvasView.scala` (`nodesWithStyles`, `edgeAddingIndicatorLine`)
- `ToolBarComponent.scala`
- `ToolBarView.scala` (AllTools map)
- `State.scala` (`clearGraph` method)
- `ControllerTests.scala`
- `MainCanvasComponentTests.scala`
- `NodeRenderTests.scala`
- `HappyPathTests.scala`

All `BasicTool(None)` → `BasicTool()` (or `BasicTool(BasicToolMode.Idle)`)  
All `BasicTool(Some(n))` → `BasicTool(BasicToolMode.AddingEdge(n))`  
All match cases like `case BasicTool(None) =>` → `case BasicTool(BasicToolMode.Idle) =>`  
All match cases like `case BasicTool(Some(edgeStart)) =>` → `case BasicTool(BasicToolMode.AddingEdge(edgeStart)) =>`

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

Rewrite `handleBasicTool` to implement the state machine described above. Use the pattern from the existing `handleMoveTool` for the dragging logic (update node position in `keyToData`).

Add a helper to compute distance:
```scala
private def distance(a: Vector2D, b: Vector2D): Double =
    math.sqrt(math.pow(a.x - b.x, 2) + math.pow(a.y - b.y, 2))

private val dragThreshold = 5.0
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasView.scala`

Update `nodesWithStyles` and `edgeAddingIndicatorLine` to match on `BasicToolMode.AddingEdge(edgeStart)` instead of `BasicTool(Some(edgeStart))`.

### Tests

**In `MainCanvasComponentTests.scala`:**
- "BasicTool - click on node enters edge-adding mode": Down on node → Up (no move) → state is `AddingEdge(node)`.
- "BasicTool - drag node moves it": Down on node → Move beyond threshold → state is `Dragging(node)`, node position updated.
- "BasicTool - small movement stays pending": Down on node → Move within threshold → state stays `PendingClickOrDrag`.
- "BasicTool - release after drag returns to Idle": Down → Move → Up → state is `Idle`.
- Existing tests for adding edges should still pass (just with updated `BasicToolMode` constructors).

---

## Step 11: Multi-Node Drag in SelectTool

**Goal:** In SelectTool, clicking and dragging a selected node moves all selected nodes together.

### Prerequisite

Step 10 must be complete (the drag-vs-click distinction pattern will be reused).

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/shared/Tool.scala`

Add `DraggingNodes(startPoint: Vector2D)` to `SelectMode`:
```scala
enum SelectMode {
    case Idle
    case DraggingBox(startPoint: Vector2D)
    case DraggingNodes(startPoint: Vector2D) // dragging selected nodes as a group
}
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

Update `handleSelectTool`:
- `Down` on a node that is already selected → enter `DraggingNodes(event.coords)` and push undo state.
- `Down` on a node that is NOT selected → select just that node and enter `DraggingNodes(event.coords)` (so you can immediately start dragging a freshly-clicked node).
- `Move` while `DraggingNodes(prevPoint)`:
  - Compute delta: `dx = event.coords.x - prevPoint.x`, `dy = event.coords.y - prevPoint.y`
  - Update the position of every node in `selectedNodes` by adding the delta
  - Update mode to `DraggingNodes(event.coords)` (so the next move is relative to this position)
- `Up` while `DraggingNodes` → return to `Idle`

```scala
case Move =>
    tool.mode match {
        case DraggingBox(startPoint) =>
            val potentialSelection = state.nodesInRect(startPoint, event.coords)
            state.copy(selectedNodes = potentialSelection)
        case DraggingNodes(prevPoint) =>
            val dx = event.coords.x - prevPoint.x
            val dy = event.coords.y - prevPoint.y
            val updatedKeyToData = state.selectedNodes.foldLeft(state.keyToData) { (ktd, nodeIdx) =>
                ktd.get(nodeIdx) match {
                    case Some(data) => ktd.updated(nodeIdx, data.copy(x = data.x + dx, y = data.y + dy))
                    case None => ktd
                }
            }
            state.copy(
                keyToData = updatedKeyToData,
                toolState = SelectTool(DraggingNodes(event.coords))
            )
        case _ => state
    }
```

### Tests

**In `MainCanvasComponentTests.scala`:**
- "SelectTool - drag selected node moves all selected nodes": Two nodes selected at (100,100) and (200,200). Down on first node, Move by (50,50). Assert both nodes moved to (150,150) and (250,250).
- "SelectTool - drag unselected node selects and moves it": Node 0 at (100,100) not selected. Down on it → it becomes selected and enters DraggingNodes. Move → position updates.
- "SelectTool - release after drag returns to Idle": Assert `SelectTool(Idle)` after Up.

---

## Step 12: Info Pane Scoped to Selection

**Goal:** When nodes are selected, the right panel (Build pane) shows info about the selected subgraph: selected node count, edge count within the selection, and the adjacency matrix highlights or filters to the induced subgraph.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/buildpane/BuildPaneComponent.scala`

Read the current file first to understand how it works. It likely sets text content for `node-count` and `edge-count` DOM elements.

When `state.selectedNodes.nonEmpty`:
- Node count display: "3 / 8" (selected / total) or "Selected: 3 of 8"
- Edge count display: count edges where both endpoints are in `selectedNodes`

When `state.selectedNodes.isEmpty`:
- Show total counts as currently done

Add a helper to `State.scala` for counting edges within a selection:
```scala
def edgesInSelection(nodes: Set[Int]): Int = {
    graph.getEdges.count { case (from, to) => nodes.contains(from) && nodes.contains(to) }
}
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/adjacencymatrix/AdjacencyMatrixComponent.scala`

This is more complex. The adjacency matrix currently renders for all nodes. When there's a selection, it should highlight the rows/columns corresponding to selected nodes, or filter to show only the selected subgraph. 

The simpler approach (highlight, don't filter): in `AdjacencyMatrixView`, when rendering cells, use a different color for cells where both row and column are in `selectedNodes`. This is purely a view change — pass `selectedNodes` to the view and adjust fill colors.

**Read the adjacency matrix view code** before implementing — the rendering logic may already have highlight concepts (it handles hover highlights) that you can extend.

### Tests

- "BuildPane shows selected count when selection exists": Create state with 5 nodes, select 2, check that the BuildPane render op contains "2 / 5" or equivalent.
- "BuildPane shows total count when no selection": Assert normal behavior unchanged.
- "edgesInSelection helper": Graph with edges (0→1), (1→2), (2→3). Selection = {0, 1, 2}. Edges in selection = 2 ((0→1) and (1→2)).

---

## Notes for the implementer

- **Build command:** `cd public/scalajs && ./mill graphcontroller.fastLinkJS`
- **Test command:** `cd public/scalajs && ./mill graphcontroller.test`
- Follow existing code patterns. Look at `MainCanvasComponentTests.scala` for test patterns.
- All logic must be in pure functions (`update`/`view`). Side effects only in `EventListener.init` and `RenderOp.render()`.
- When in doubt about a decision, leave a `// TODO` comment rather than guessing.
- Steps are designed to be implemented sequentially. Steps 5–9 are independent of each other and could be done in any order. Step 10 should come before Step 11 (the drag-vs-click pattern is established in 10 and reused in 11). Step 12 can be done at any point after Step 3.
- After implementing any step, run all tests to check for regressions.
- **Do not remove MoveTool.** Add the TODO comment mentioned at the top of this document.
