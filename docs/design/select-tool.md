# SelectTool Design

**Status:** Proposed  
**Date:** 2026-04-13  
**Context:** User interviews revealed that people instinctively try to click and drag nodes to reposition them. The SelectTool should make selection and movement a single fluid gesture.

## Core Principle

Selection and movement should feel like Figma/Sketch — the pointer tool is the natural default for selecting, moving, and manipulating existing elements.

## Selection State

`selectedNodes: Set[Int]` belongs on `State` (not inside `SelectTool`), because:
- The info pane, adjacency matrix, and export pane all need to read it.
- Selection should persist across tool switches (e.g., select nodes, switch to BasicTool to add an edge, switch back — selection is still there).

## Selection Gestures (within SelectTool)

| Gesture | Behavior |
|---------|----------|
| Click empty canvas | Clear selection |
| Click a node | Select that single node (clear previous) |
| Shift+click a node | Toggle that node in/out of selection |
| Click+drag from empty space | Draw selection rectangle, select all nodes inside on release |
| Shift+click+drag | Add to existing selection (don't clear first) |
| Click+drag a selected node | Move all selected nodes together |
| Click+drag an unselected node | Select just that node and start moving it |

## Merge MoveTool into SelectTool

The current `MoveTool` is a strict subset of SelectTool (click a node and drag it). Deprecate MoveTool entirely — SelectTool = select + move. This reduces toolbar clutter.

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| `V` | Switch to SelectTool (industry standard for pointer/select) |
| `B` | Switch to BasicTool |
| `Esc` | In SelectTool: clear selection. In other tools: switch back to BasicTool |
| `Delete`/`Backspace` | Delete selected nodes and their incident edges |

**Why not Esc to enter SelectTool?** No app uses Esc to *enter* a mode. Esc universally means "cancel/deselect/exit."

## BasicTool Node Dragging

In BasicTool, click+drag on an existing node should move it directly — no tool switch, no visual indicator change:
- Mouse down on empty space: add node (existing behavior)
- Mouse down on a node, release without moving: enter edge-adding mode (existing behavior)
- Mouse down on a node, drag: move the node (new — absorbs single-node movement from MoveTool)

This gives BasicTool both building *and* individual repositioning. SelectTool handles *multi-node* selection and movement.

## Info Pane Reflects Selection

When `selectedNodes` is non-empty, the right panel should scope to the selection:
- "Selected: 3 of 8 nodes"
- Adjacency matrix highlights or filters to the induced subgraph
- Edge count shows edges within the selection

This is view-only — `BuildPaneComponent` and `AdjacencyMatrixComponent` read `state.selectedNodes` and adjust rendering.

## Double-Click

Double-click on empty space adds a node (staying in SelectTool). This lets users build without switching to BasicTool. Label editing (if added later) could use right-click or `F2`.

## SelectTool Internal State

The `SelectTool` case class needs richer state than `mousePressedStartPoint`. Use a sealed trait:

```
sealed trait SelectMode
case object Idle extends SelectMode
case class DraggingBox(startPoint: Vector2D) extends SelectMode
case class DraggingNodes(startPoint: Vector2D) extends SelectMode
```

## Visual Feedback

- Selection rectangle: drawn while dragging (already partially implemented in `MainCanvasView.selectionBox`)
- Selected nodes: distinct highlight ring or bounding box per node (persists after mouse-up). Add a `Selected` case to `NodeRenderStyle`.

## Implementation Notes

- Reuse `isInside` from `MainCanvasComponent` for rectangular hit-testing
- `DeleteSelectedNodes` event + corresponding `State` method needed for delete key support
- `selectedNodes` should be cleared when switching to a tool that doesn't support selection (or kept — TBD based on feel)
