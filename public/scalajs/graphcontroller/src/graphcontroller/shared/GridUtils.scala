package graphcontroller.shared

import scala.math.round

/**
 * A utility class to pre-calculate the integer coordinates for a grid.
 * This is used to ensure that all parts of the grid (lines, cells, highlights)
 * are rendered without gaps or overlaps.
 *
 * @param width The total width of the grid area.
 * @param height The total height of the grid area.
 * @param nodeCount The number of rows/columns in the grid.
 */
case class GridUtils(width: Int, height: Int, nodeCount: Int) {
  // Calculate the x and y coordinates for each grid line, distributing the pixels as evenly as possible.
  val colCoords: Seq[Int] = (0 to nodeCount).map(i => round(width.toDouble * i / nodeCount).toInt)
  val rowCoords: Seq[Int] = (0 to nodeCount).map(i => round(height.toDouble * i / nodeCount).toInt)

  /** Returns the starting x-coordinate for the given column index. */
  def getX(col: Int): Int = colCoords(col)

  /** Returns the starting y-coordinate for the given row index. */
  def getY(row: Int): Int = rowCoords(row)

  /** Returns the width of the given column index. */
  def getWidth(col: Int): Int = colCoords(col + 1) - colCoords(col)

  /** Returns the height of the given row index. */
  def getHeight(row: Int): Int = rowCoords(row + 1) - rowCoords(row)
}
