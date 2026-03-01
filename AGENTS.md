This application is a single web page made for building graphs (as in graph theory, with nodes and edges).

### Key Directives
*   **All new code must use ScalaJS and the Components model.**
*   The core logic must be in pure functions within `Controller.handleEventWithState` and `Component` methods (`update`/`view`).
*   Side-effects are only allowed at the "edges": in `EventListener`s (reading from the DOM) and `RenderOp.render()` methods (writing to the DOM).
*   The `original_vanilla_js_code.js` file is considered legacy and the goal is to continually migrate its functionality into the ScalaJS Components model.

### Common Tasks
*   **Run the application locally:** `sh start.sh`
*   **Build for development:** `cd public/scalajs && ./mill graphcontroller.fastLinkJS`
*   **Run tests:** `cd public/scalajs && ./mill graphcontroller.test`

### Development Workflow: Adding a Feature

This section provides a step-by-step pattern for adding a new feature. It also serves as a practical introduction to the core architectural concepts.

**Example Scenario:** Adding a new button that clears the graph and displays a confirmation message.

#### 1. Define the Event
First, define an `Event` to represent the user's action. An `Event` is an immutable case class or object that describes what happened.

*In `public/scalajs/graphcontroller/src/graphcontroller/controller/Event.scala`:*
```scala
case object ClearGraphClicked extends Event
```

#### 2. Add Logic to `State`
Next, modify the `State` to handle the consequences of the event. The `State` is an immutable case class that is the single source of truth for the entire application. We'll add a field for our confirmation message and a helper method to produce the new state.

*In `public/scalajs/graphcontroller/src/graphcontroller/model/State.scala`:*
```scala
// Add a field to hold the message
case class State(
  // ... other fields
  statusMessage: Option[String] = None
) {
  // Add a helper method to produce the new state
  def clearGraph: State = this.copy(
    graph = new DirectedMapGraph[Int](),
    keyToData = Map.empty,
    statusMessage = Some("Graph cleared!")
  )
}
```

#### 3. Create the `Component`
A `Component` is a logical piece of the UI with pure functions for updating state and describing view changes. Create a new component file to handle the `ClearGraphClicked` event.

*In a new file, e.g., `ClearButtonComponent.scala`:*
```scala
object ClearButtonComponent extends Component {
  // The `update` function is pure: (State, Event) => New State
  override def update(state: State, event: Event): State = {
    event match {
      case ClearGraphClicked => state.clearGraph
      case _ => state
    }
  }

  // The `view` function is pure: State => RenderOp
  // It describes a UI change based on the new state.
  override def view(state: State): RenderOp = {
    state.statusMessage match {
      case Some(message) => SetTextContent("status-message", message)
      case None => NoOp // NoOp is a RenderOp that does nothing
    }
  }
}
```

#### 4. Define the `RenderOp`
The `view` method returns a `RenderOp`, which is a self-contained object that describes and performs a side-effect. By returning a data object (`SetTextContent`) instead of directly changing the DOM, the `Component` remains pure and testable.

Here is the full definition for the `SetTextContent` `RenderOp` we just used. Its `render()` method contains the minimal, side-effectful DOM manipulation code.

*In a shared file, e.g. `ops/DOMOps.scala`:*
```scala
case class SetTextContent(id: String, text: String) extends RenderOp {
  def render(): Unit = {
    // This is the side-effect at the "edge" of the program.
    val elem = dom.document.getElementById(id)
    if (elem != null) elem.textContent = text
  }
}
```
*(Other useful RenderOps like `SetAttribute` or `NoOp` can be defined similarly.)*

#### 5. Register the `Component` and `EventListener`
Finally, wire up the new code. The central **`Controller`** orchestrates all components, and the **`EventListener`** provides the initial input from the DOM.

*In `public/scalajs/graphcontroller/src/graphcontroller/controller/Controller.scala`, register the component:*
```scala
private val components: Seq[Component] = Seq(
    // ...,
    ClearButtonComponent
)
```

*In `public/scalajs/graphcontroller/src/graphcontroller/Main.scala`, register the listener:*
```scala
// In a new file, e.g. ClearButtonEventListeners.scala
object ClearButtonEventListeners extends EventListener {
  override def init(dispatch: Event => Unit): Unit = {
    val element = dom.document.getElementById("clear-graph-btn")
    if (element != null) {
      element.addEventListener("click", _ => dispatch(ClearGraphClicked))
    }
  }
}

// In Main.scala
private val eventListeners: Seq[EventListener] = Seq(
    // ...,
    ClearButtonEventListeners
)
```

#### 6. Write a Test
Verify the pure logic in `ControllerTests.scala`.

*In `public/scalajs/graphcontroller/test/src/ControllerTests.scala`:*
```scala
test("Clearing the graph") {
  val stateWithNode = State.init.addNode(Vector2D(10,10))
  val (newState, renderOps) = Controller.handleEventWithState(ClearGraphClicked, stateWithNode)
  assert(newState.graph.nodeCount == 0)
  assert(newState.statusMessage == Some("Graph cleared!"))

  val setTextOp = renderOps.collectFirst {
    case op: SetTextContent if op.id == "status-message" => op
  }
  assert(setTextOp.isDefined)
  assert(setTextOp.get.text == "Graph cleared!")
}
```

### Testing Guidelines

When writing unit tests for features implemented with the Components model, follow these rules:

1.  **Test the Pure Logic:** Use `Controller.handleEventWithState(event, state)` instead of `Controller.handleEvent(event)`. 
2.  **Avoid DOM Dependencies:** `handleEvent` executes side-effects (`RenderOp.render()`) which will fail in a test environment (like Node.js) where `document` or `window` are not defined.
3.  **Assert on State and RenderOps:** Verify that the returned `newState` is correct and that the expected `Seq[RenderOp]` objects are present with the correct data.

### Architecture Deep Dive

#### Data Flow
The architecture is designed to maximize testability by isolating program logic into pure functions and pushing side-effects to the "edges". The central pure function is `Controller.handleEventWithState`.

The data flow is as follows:

1.  An **EventListener** captures a DOM event and creates an **Event** object. This is an impure "read" from the DOM.
2.  The `EventListener` dispatches the `Event` to **`Controller.handleEvent`**.
3.  `Controller.handleEvent` immediately calls the pure function **`Controller.handleEventWithState`**.
4.  `handleEventWithState` calculates the new **State** by calling the pure `update` method on all registered **Components**.
5.  It then calculates the necessary UI changes by calling the pure `view` method on all **Components**, returning a sequence of **RenderOp** objects.
6.  The new `State` and the `Seq[RenderOp]` are returned to `Controller.handleEvent`.
7.  `Controller.handleEvent` saves the new `State` and then calls the `render()` method on each `RenderOp`.
8.  The **`RenderOp.render()`** method performs the final, minimal side-effect. This is an impure "write" to the DOM.
