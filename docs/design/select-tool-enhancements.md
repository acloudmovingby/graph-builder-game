# SelectTool Enhancements: Complete Edges & Click-to-Narrow Selection

**Parent design doc:** [select-tool.md](select-tool.md)  
**Date:** 2026-04-14

## Overview

Two enhancements to the SelectTool:

1. **Return key completes all edges in the selection** — pressing Enter/Return adds every possible edge between the currently selected nodes (same logic as AreaCompleteTool, but triggered by keyboard on an existing selection).
2. **Click-to-narrow selection** — clicking a node that's already part of a multi-node selection narrows the selection down to just that node. Currently this is swallowed by the DraggingNodes behavior.

---

## Feature 1: Return Key Completes Edges in Selection

### Behavior

When the user presses Enter/Return and there are 2+ selected nodes, add all edges between every pair of selected nodes. This should be undoable (push undo state before the mutation). If there are 0 or 1 selected nodes, do nothing.

This is exactly the same edge-completion logic as `AreaCompleteTool`'s mouse-up handler (`MainCanvasComponent.scala` lines ~196-208), but triggered from a key press rather than a drawn polygon.

### Files to change

**`Event.scala`** — Add a new event:

```scala
case object CompleteSelectedEdges extends Event
```

**`KeyboardShortcutListeners.scala`** — Add an Enter key handler. In the existing `keydown` listener, add a case for `e.key == "Enter"` that dispatches `CompleteSelectedEdges`. The same guards already in place (skip if target is input/textarea/select, skip if modifier key held) should apply here too.

**`MainCanvasComponent.scala`** — Handle the new event in the `update` method. Add a case alongside the existing `DeleteSelectedNodes` handler:

```scala
case CompleteSelectedEdges if state.selectedNodes.size >= 2 =>
    val nodes = state.selectedNodes.toSeq
    val edges = for {
        a <- nodes
        b <- nodes
        if a != b
    } yield (a, b)
    state.pushUndoState.bulkUpdateEdges(edges, isAdd = true)
```

Note: `bulkUpdateEdges` already handles the case where some edges already exist (it's idempotent for adds). It also handles directed vs undirected graphs correctly.

### Tests to write

In `MainCanvasComponentTests.scala` (or `ControllerTests.scala`):

1. **CompleteSelectedEdges with 3 selected nodes adds all 6 directed edges (or 3 undirected).** Set up a state with 3 nodes, no edges, `selectedNodes = Set(0, 1, 2)`. Fire `CompleteSelectedEdges`. Assert all pairs have edges.
2. **CompleteSelectedEdges with < 2 selected nodes is a no-op.** Fire with 0 and 1 selected nodes. Assert state unchanged.
3. **CompleteSelectedEdges is undoable.** Fire the event, then fire `UndoRequested`. Assert edges are gone.
4. **Idempotent: firing twice doesn't break anything.** Complete edges, complete edges again. Assert same state.

---

## Feature 2: Click-to-Narrow Selection

### The problem

In `handleSelectTool`, when you click (Down) on a node that's already in the selection, the code enters `DraggingNodes` and preserves the full selection. This is correct for dragging — you want to drag the whole group. But if the user just clicks (Down then Up with no movement), the intent is to select just that one node. Currently nothing visible happens because the selection doesn't change and the drag has zero displacement.

### Solution: narrow on Up, not on Down

The standard pattern (used by Figma, Illustrator, etc.): keep the full selection on Down (so dragging works), but narrow to a single node on Up **if no movement occurred**.

To detect "no movement", we need to know whether any `Move` events fired between the Down and Up. The simplest approach: add a `hasMoved: Boolean` flag to `DraggingNodes`.

### Files to change

**`Tool.scala`** — Add a `hasMoved` flag to `DraggingNodes`:

```scala
case DraggingNodes(lastPoint: Vector2D, hasMoved: Boolean = false)
```

**`MainCanvasComponent.scala`** — Three changes in `handleSelectTool`:

1. **Down on an already-selected node** — no change needed. It already enters `DraggingNodes(event.coords)` which will now default `hasMoved = false`.

2. **Move in DraggingNodes** — set `hasMoved = true`. Change the existing line:

```scala
// Before:
state.copy(keyToData = updatedKeyToData, toolState = SelectTool(DraggingNodes(event.coords)))
// After:
state.copy(keyToData = updatedKeyToData, toolState = SelectTool(DraggingNodes(event.coords, hasMoved = true)))
```

3. **Up in DraggingNodes** — if `hasMoved` is false, narrow the selection. The Up handler currently just resets to Idle:

```scala
// Before:
case _ =>
    state.copy(toolState = SelectTool(Idle))
// After:
case DraggingNodes(lastPoint, hasMoved) if !hasMoved =>
    // Click without drag: narrow selection to just the clicked node
    val clickedNode = hoveredNode(event.coords, state.keyToData)
    clickedNode match {
        case Some(node) => state.copy(toolState = SelectTool(Idle), selectedNodes = Set(node))
        case None => state.copy(toolState = SelectTool(Idle))
    }
case _ =>
    state.copy(toolState = SelectTool(Idle))
```

Note: we call `hoveredNode` again on Up rather than storing which node was clicked on Down. This is fine because the node hasn't moved (hasMoved is false), so the hit-test will find the same node. If the cursor somehow drifted off the node between Down and Up (unlikely but possible), we just go to Idle without narrowing, which is safe.

### Edge case: Down on an already-selected node should NOT pushUndoState if no drag happens

Currently, Down on a node always calls `state.pushUndoState` (line 70). This means a simple click (that narrows selection) will push an unnecessary undo entry where nothing changed. Fix: only push undo state when movement actually starts.

Move `pushUndoState` from Down to the first Move:

```scala
// In the Move handler, DraggingNodes case:
case DraggingNodes(lastPoint, hasMoved) =>
    val dx = event.coords.x - lastPoint.x
    val dy = event.coords.y - lastPoint.y
    val updatedKeyToData = state.selectedNodes.foldLeft(state.keyToData) { (ktd, nodeIdx) =>
        ktd.get(nodeIdx) match {
            case Some(data) => ktd.updated(nodeIdx, data.copy(x = (data.x + dx).toInt, y = (data.y + dy).toInt))
            case None => ktd
        }
    }
    val stateBeforeMove = if (!hasMoved) state.pushUndoState else state
    stateBeforeMove.copy(keyToData = updatedKeyToData, toolState = SelectTool(DraggingNodes(event.coords, hasMoved = true)))
```

And remove the `.pushUndoState` from the Down handler (line 70):

```scala
// Before:
state.pushUndoState.copy(toolState = SelectTool(DraggingNodes(event.coords)), selectedNodes = newSelection)
// After:
state.copy(toolState = SelectTool(DraggingNodes(event.coords)), selectedNodes = newSelection)
```

### Tests to write

In `MainCanvasComponentTests.scala`:

1. **Click (Down+Up, no Move) on a node in a multi-node selection narrows to that node.** Set up state with 3 nodes, `selectedNodes = Set(0, 1, 2)`. Fire Down on node 1, then Up on node 1 (no Move in between). Assert `selectedNodes == Set(1)`.
2. **Drag (Down+Move+Up) on a node in a multi-node selection preserves the full selection.** Same setup, but fire Down, Move (to a different coord), Up. Assert `selectedNodes` still contains all 3.
3. **Click-to-narrow does NOT push an undo state.** Fire Down+Up with no Move. Assert undo stack length is unchanged.
4. **Drag DOES push an undo state (on first Move).** Fire Down, Move, Up. Assert undo stack grew by 1.
5. **Click on a node NOT in the selection still replaces selection (existing behavior preserved).** State with `selectedNodes = Set(0, 1)`. Click on node 2. Assert `selectedNodes == Set(2)`.

---

## Implementation Order

Do Feature 1 first — it's smaller and fully independent. Feature 2 is a bit more involved due to the `hasMoved` flag threading and the undo-push relocation, but it's self-contained within `handleSelectTool`.

| Order | Feature | Complexity | Key risk |
|-------|---------|-----------|----------|
| 1 | Return key completes edges | Small | None — reuses existing `bulkUpdateEdges` |
| 2 | Click-to-narrow selection | Medium | Must not break drag behavior or undo semantics |
