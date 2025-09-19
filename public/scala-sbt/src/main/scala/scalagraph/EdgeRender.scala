package scalagraph

case class Point(x: Int, y: Int)

case class CanvasLine(
  from: Point,
  to: Point,
  width: Int,
  color: String // Hex string, e.g. "#FF0000"
)

