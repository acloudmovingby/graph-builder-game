This application is a single web page made for building graphs (as in graph theory, with nodes and edges). 

## How to build
Look at the file README.md in the top level of repo. It has instructions on how to run/test.

## Vanilla JS (original implementation)
The original program was written in vanilla Javascript but is in the process of being ported to ScalaJS.
It is mostly contained in the file called (at time of writing) original_vanilla_js_code.js. It was written all in one big file.

### Migration State
Currently about half of functionality has been ported. This timeline might help explain the strange organization:
- Originally it was all written in one vanilla JS file
- Then I decided to do the underlying graph theory stuff in ScalaJS because it's more robust. So the vanilla JS code called GraphController.scala
- When I added the adjacency matrix stuff, I decided to make the whole interaction, from event listeners down to the canvas drawing, all done in ScalaJS. I did it in an FP way with side effects pushed out to the extreme ends and everything going through a single pipeline.
- So now we have two ways the core state is changed: 
    - Vanilla JS input events modify state and read from state to draw some things
    - FP pipeline (through Controller.scala) changes state and uses it to draw things

## ScalaJS Code Organization
The ScalaJS code is organized as follows (Controller roughly oversees):
```
Event Listeners -> Controller -> { Model -> View -> ViewUpdater }
```

### Important Classes:
- Controller: (side-effectful) It contains all the state and is in charge of orchestrating the state change.
- Event Listeners: (side-effectful) They take dom events and turn them into the Event type to pass to the Controller. This separates core business logic from the raw dom input
- Model: Pure functions that take an existing state and an input event and calculate a new state
- View: Pure functions that take a state and create a 'view state', which is the raw data needed to render stuff to the screen (e.g. the raw geometric data for a canvas, or some text to display, etc.) 
- ViewUpdater: (side effectful) Actually will render the view state changes to the view. For instance, it will actually draw frames to the html canvas api.

#### Notes:
- The Model takes a previous state and an input event, but View is purely derivative: it only takes the new model state, not the new model state and the old view state. 
- Both the vanilla JS code and the Scala code are affecting the core state right now. In the long run, we don't want that, we want everything to go through this new flow. 

## New code should use ScalaJS in Controller Pipeline model
The goal is to get rid off of all the vanilla JS code and use Scala end-to-end. The current plan is to organize it like we do with the adjacency matrix code using a single central pipeline.

There will not be new State or View classes made. Instead, the existing Model/View/ViewUpdater, etc. code will be added to (e.g. add more parameters to the State case class).
