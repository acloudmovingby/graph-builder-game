package graphcontroller.model.adjacencymatrix

/**
 * Represents a cell in the adjacency matrix. (Row X in the matrix represents all edges going _from_ node X to other nodes)
 * I kept getting confused when using (Int, Int) tuples everywhere, so making this into its own type makes the code clearer.
 *
 * TODO: make an Edge class and rename this to MatrixCell or something?
 * */
case class Cell(row: Int, col: Int) {
	def toEdge: (Int, Int) = (row, col)
}
object Cell {
  def fromEdge(from: Int, to: Int): Cell= Cell(from, to)
}
