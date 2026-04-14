# SelectTool Implementation Plan

**Parent design doc:** [select-tool.md](select-tool.md)  
**Date:** 2026-04-13

## Open Questions / Things to Think About

Before diving in, a few things surfaced during detailed analysis:

1. **Keyboard shortcuts and text inputs.** Bare letter keys (`V`, `B`) will fire even when the user is focused on an input or select element (e.g. the export format dropdown). The `keydown` listener must check `e.target` and bail if it's an input/select/textarea. The existing undo shortcut (`Cmd+Z`) doesn't have this problem because modifier keys don't conflict with typing.

2. **`selectedNodes` must NOT go in the undo stack.** `HistoricalState` only stores `graph` and `keyToData`. If `selectedNodes` were part of `HistoricalState`, undoing an edge addition would also undo a selection change, which would feel broken. Selection is a UI/interaction concern, not a data concern. Keep it on `State` but outside of `pushUndoState`/`HistoricalState`.

3. **Don't reuse `isInside` for rectangle hit-testing.** `isInside` does ray-casting for arbitrary polygons. A rectangle check is just `min/max` bounds comparison — simpler, faster, and more readable. Write a dedicated helper.

4. **Selected + Hovered style priority.** When a node is both selected and hovered, what does it look like? The `nodesWithStyles` function currently produces one style per node. We need to decide: does `Selected` override hover, or is there a `SelectedHover` style? For the first pass, `Selected` can just override — keep it simple.

5. **Selection invalidation.** When the user adds or removes a node, node indices can shift. For now, clearing `selectedNodes` on any graph mutation (add node, add edge via BasicTool, undo/redo) is the safest approach. We can refine later.

---

## Implementation Order

| Step | What | Complexity |
|------|------|-----------|
| 1 | Keyboard shortcuts (V, B, Esc) | Small |
| 2 | SelectTool internal state refactor (SelectMode) | Small |
| 3 | `selectedNodes` on State + rectangle selection logic | Medium |
| 4 | Visual feedback for selected nodes | Medium |
| 5+ | Delete key, BasicTool drag, info pane scoping, MoveTool deprecation | Future |

---

## Step 1: Keyboard Shortcuts

**Goal:** Press `V` to switch to SelectTool, `B` to switch to BasicTool, `Esc` to clear selection (once it exists) or reset current tool.

### Files to change

**New file:** `public/scalajs/graphcontroller/src/graphcontroller/components/toolbar/eventlisteners/KeyboardShortcutListeners.scala`

- Create a new `EventListener` object (following the pattern in `UndoEventListeners.scala`).
- Register a single `document.addEventListener("keydown", ...)` handler.
- **Critical:** Guard against firing in text inputs. Check `e.target` — if it's an `HTMLInputElement`, `HTMLTextAreaElement`, or `HTMLSelectElement`, return early without dispatching.
- Dispatch existing `ToolSelected("select")` for `V`, `ToolSelected("basic")` for `B`.
- For `Esc`: dispatch a new `EscapePressed` event (don't dispatch `ToolSelected` — Esc behavior is context-dependent and will do different things depending on tool state).
- Do not check for modifier keys (Cmd/Ctrl/Shift) — these shortcuts are bare keys only. In fact, do NOT fire if a modifier is held (so `Cmd+V` for paste doesn't switch tools).

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/controller/Event.scala`

- Add `case object EscapePressed extends Event`.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/Main.scala`

- Add `KeyboardShortcutListeners` to the `eventListeners` Seq.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/toolbar/ToolBarComponent.scala`

- In `update`, add a case for `EscapePressed`:
  - If current tool is `SelectTool`: for now, just reset to `SelectTool(Idle)` (clearing selection comes in Step 3).
  - If current tool is anything else: switch to `BasicTool(None)`.

### Tests

In `ControllerTests.scala` or a new `KeyboardShortcutTests.scala`:
- `EscapePressed` while in `BasicTool` → state stays `BasicTool(None)` (already default).
- `EscapePressed` while in `MagicPathTool(Some(3))` → state becomes `BasicTool(None)`.
- `EscapePressed` while in `AreaCompleteTool(true, points)` → state becomes `BasicTool(None)`.
- `ToolSelected("select")` → state becomes `SelectTool(...)`.
- `ToolSelected("basic")` → state becomes `BasicTool(None)`.

Note: we can't unit test the input-focus guard (that's DOM-level), but the pure event handling is fully testable.

---

## Step 2: SelectTool Internal State Refactor

**Goal:** Replace `SelectTool(mousePressedStartPoint: Option[Vector2D])` with a richer state model using a sealed trait.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/shared/Tool.scala`

Replace:
```scala
case class SelectTool(mousePressedStartPoint: Option[Vector2D]) extends Tool {
```

With:
```scala
case class SelectTool(mode: SelectMode = SelectMode.Idle) extends Tool {
```

And add the sealed trait (in the same file or a new file — same file is fine since it's small):
```scala
enum SelectMode {
  case Idle
  case DraggingBox(startPoint: Vector2D)
  // DraggingNodes will be added in a future step
}
```

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

Update `handleSelectTool` to match on `SelectMode` instead of `Option[Vector2D]`:
- `Down` on empty canvas → `SelectTool(DraggingBox(coords))`
- `Up` → `SelectTool(Idle)` (selection computation comes in Step 3)
- `Move` while `DraggingBox` → no state change needed (the view reads `lastMousePosition` for the box corner)
- `Leave` → `SelectTool(Idle)`

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasView.scala`

Update `selectionBox` to match on `SelectTool(DraggingBox(start))` instead of `SelectTool(Some(start))`.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/toolbar/ToolBarComponent.scala`

Update the `ToolSelected("select")` case to use `SelectTool(SelectMode.Idle)` instead of `SelectTool(mousePressedStartPoint = None)`.

**Grep for all other references** to `SelectTool(` across the codebase and update them. Key places:
- `State.scala` line 109 (`clearGraph` resets to `BasicTool`, fine)
- `ToolBarComponent.scala`
- Any test files

### Tests

- Existing `MainCanvasComponentTests` should still pass after updating `SelectTool` construction.
- Add: `SelectTool` drag box lifecycle — Down at (10,10) → state is `DraggingBox(10,10)` → Up → state is `Idle`.

---

## Step 3: `selectedNodes` on State + Rectangle Selection

**Goal:** When the user drags a rectangle in SelectTool and releases, compute which nodes are inside and store them in `state.selectedNodes`.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/model/State.scala`

- Add `selectedNodes: Set[Int] = Set.empty` to the `State` case class.
- Add to `State.init` with default `Set.empty`.
- **Do NOT add it to `HistoricalState`.** Selection is not undoable.
- Add a helper method for rectangle hit-testing:
  ```scala
  def nodesInRect(topLeft: Vector2D, bottomRight: Vector2D): Set[Int] = {
    val minX = math.min(topLeft.x, bottomRight.x)
    val maxX = math.max(topLeft.x, bottomRight.x)
    val minY = math.min(topLeft.y, bottomRight.y)
    val maxY = math.max(topLeft.y, bottomRight.y)
    keyToData.collect {
      case (key, data) if data.x >= minX && data.x <= maxX && data.y >= minY && data.y <= maxY => key
    }.toSet
  }
  ```
  Note: use `min`/`max` because the user can drag in any direction (right-to-left, bottom-to-top).

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasComponent.scala`

Update `handleSelectTool`:
- On `Up` while in `DraggingBox(startPoint)`:
  - Call `state.nodesInRect(startPoint, event.coords)` to get selected nodes.
  - Return `state.copy(toolState = SelectTool(Idle), selectedNodes = selectedNodes)`.
- On `Down` on empty canvas (no hovered node):
  - Clear selection: `state.copy(selectedNodes = Set.empty, toolState = SelectTool(DraggingBox(coords)))`.
- On `Down` on a node (hovered node exists):
  - Select just that node: `state.copy(selectedNodes = Set(hoveredNode), toolState = SelectTool(Idle))`.
  - (Shift+click toggling is a future enhancement — needs modifier key info in events.)

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/toolbar/ToolBarComponent.scala`

Update `EscapePressed` handling for `SelectTool`:
- If `selectedNodes` is non-empty: clear selection, stay in SelectTool.
- If `selectedNodes` is empty: switch to `BasicTool(None)`.

This gives Esc a two-stage behavior: first press clears selection, second press exits SelectTool. This is more intuitive than a single press doing both.

**Consider:** Should other tool state changes (switching tools via toolbar click or keyboard) clear `selectedNodes`? For now, yes — clear it whenever the tool changes to a non-SelectTool. This avoids stale selection state bleeding into other tools. Add this to `ToolBarComponent.update` in the `ToolSelected` handler.

### Tests

- `nodesInRect` helper: nodes at (10,10) and (20,20), rect from (5,5) to (25,25) → both selected. Node at (100,100) → not selected.
- `nodesInRect` with reverse-direction drag: rect from (25,25) to (5,5) → same result (min/max handles it).
- Full SelectTool lifecycle via `handleEventWithState`: Down → Move → Up → verify `selectedNodes` is correct.
- Click on empty canvas → `selectedNodes` is empty.
- Click on a node → `selectedNodes` is `Set(thatNode)`.
- `EscapePressed` with non-empty selection → selection cleared, still in SelectTool.
- `EscapePressed` with empty selection → switched to BasicTool.

---

## Step 4: Visual Feedback for Selected Nodes

**Goal:** Selected nodes get a distinct visual style (highlight ring) that persists after the selection rectangle disappears.

### Files to change

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/NodeRender.scala`

Add a new case to `NodeRenderStyle`:
```scala
enum NodeRenderStyle {
  case Basic,
  BasicHover,
  AddEdgeStart,
  AddEdgeNotStart,
  AddEdgeHover,
  AddEdgeHoverStart,
  Selected       // <-- new
}
```

Add rendering for `Selected` in `createNodeCanvasObject`. Suggestion: use the existing `basicNodeCircle` with a dashed or colored ring around it (similar to `basicHover` but with a distinct color, e.g. a green or orange ring, or a dashed blue ring to differentiate from hover). Keep it simple — a solid ring in a new color is enough:
```scala
case Selected =>
  val node = basicNodeCircle(center)
  val selectionRing = CircleCanvas(
    circ = Circle(center = center, radius = baseNodeRadius + 6),
    fillColor = None,
    borderColor = Some(color1),  // or a new selection-specific color
    borderWidth = Some(2.0),
    lineDashSegments = Seq(4, 4)  // dashed ring to distinguish from hover
  )
  Seq(node, selectionRing) ++ basicText
```

A dashed ring distinguishes "selected" from "hovered" clearly. Hover = solid ring. Selected = dashed ring. Both = could just show the dashed ring (selected takes priority), or both rings overlaid. For the first pass, selected overrides hover — this avoids a combinatorial explosion of styles.

**Change:** `public/scalajs/graphcontroller/src/graphcontroller/components/maincanvas/MainCanvasView.scala`

Update `nodesWithStyles` to account for `selectedNodes`. This is the key change. The function signature currently takes `(nodes, hoveringOnNode, toolState)` — it needs access to `selectedNodes` too.

Option A: Add `selectedNodes: Set[Int]` as a parameter.  
Option B: Pass the whole `State`.

Option A is cleaner (keeps the function's inputs explicit). Update the call site in `nodes(state)` to pass `state.selectedNodes`.

Inside `nodesWithStyles`, after the existing tool-specific logic, override the style for any node in `selectedNodes`:
```scala
// After computing styles from tool state, override selected nodes
val finalStyles = baseStyles.map {
  case (node, _) if selectedNodes.contains(node) => (node, Selected)
  case other => other
}
```

This means selection overrides all other styles. Simple rule, easy to reason about.

**Change the call site** in `MainCanvasView.nodes(state)`:
```scala
nodesWithStyles(state.graph.nodes, state.canvasInteraction.hoveredNode, state.toolState, state.selectedNodes)
```

### Tests

**In `NodeRenderTests.scala`:**
- Node in `selectedNodes` → gets `Selected` style regardless of tool state.
- Node not in `selectedNodes` → gets normal style.
- Hovered node that's also selected → gets `Selected` (override).

---

## Notes for the implementer

- **Build command:** `cd public/scalajs && ./mill graphcontroller.fastLinkJS`
- **Test command:** `cd public/scalajs && ./mill graphcontroller.test`
- Follow existing code patterns. Look at `UndoEventListeners.scala` for the keyboard listener pattern, `MainCanvasComponentTests.scala` for test patterns.
- All logic must be in pure functions (`update`/`view`). Side effects only in `EventListener.init` and `RenderOp.render()`.
- When in doubt about a decision, leave a `// TODO` comment rather than guessing.
