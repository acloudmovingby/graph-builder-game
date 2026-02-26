// =====================
// State and Constants
// =====================
let canvas = document.getElementById("main-canvas-upper");
const infoPaneWidth = document.getElementsByClassName("right-pane")?.[0].offsetWidth;
let printCounter = 0;
let scale = window.devicePixelRatio;

// =====================
// Canvas Setup
// =====================
// TODO: Things don't work if I don't do this, but I suspect if I set up the css properly, this shouldn't be necessary?
// (except for the scale issue, I'm not sure that can be solved with css alone)
function setCanvasSize() {
    const canvasWidth = window.innerWidth - infoPaneWidth;
    const canvasHeight = window.innerHeight;
    canvas.style.width = canvasWidth + "px";
    canvas.style.height = canvasHeight + "px";

    // Set actual canvas size to scaled size for high-DPI displays (keeps edges looking sharp)
    canvas.width = canvasWidth * scale;
    canvas.height = canvasHeight * scale;
    if (canvas.getContext) {
        let ctx = canvas.getContext("2d");
        ctx.scale(scale, scale);
    }
}
setCanvasSize();

// =====================
// Event Listeners
// =====================
window.addEventListener('resize', function(event) {
    setCanvasSize()
});

// Info/Export Pane Event Handlers
document.getElementById("export-pane-select").addEventListener(
    "click",
    () => {
        for (const elem of document.getElementsByClassName("info-pane-only")) {
            elem.style.display = "none";
        }
        for (const elem of document.getElementsByClassName("export-pane-only")) {
            elem.style.display = "block";
        }
        document.getElementById("export-pane-select").style.color = "black";
        document.getElementById("export-pane-select").style.fontWeight = "bold";
        document.getElementById("info-pane-select").style.color = "gray";
        document.getElementById("info-pane-select").style.fontWeight = "normal";
    },
    false
);

document.getElementById("info-pane-select").addEventListener(
    "click",
    () => {
        for (const elem of document.getElementsByClassName("export-pane-only")) {
            elem.style.display = "none";
        }
        for (const elem of document.getElementsByClassName("info-pane-only")) {
            elem.style.display = "block";
        }
        document.getElementById("info-pane-select").style.color = "black";
        document.getElementById("info-pane-select").style.fontWeight = "bold";
        document.getElementById("export-pane-select").style.color = "gray";
        document.getElementById("export-pane-select").style.fontWeight = "normal";
    },
    false
);