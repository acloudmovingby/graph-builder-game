package graphcontroller.dataobject

/**
 * Represents either a Cell or some area around the matrix
 */
sealed trait AdjMatrixZone

/**
 * Represents a cell in the adjacency matrix. (Row X in the matrix represents all edges going _from_ node X to other nodes)
 * I kept getting confused when using (Int, Int) tuples everywhere, so making this into its own type makes the code clearer.
 *
 * TODO: make an Edge class and rename this to MatrixCell or something?
 * */
case class Cell(row: Int, col: Int) extends AdjMatrixZone {
	def toEdge: (Int, Int) = (row, col)
}

object Cell {
	def fromEdge(from: Int, to: Int): Cell = Cell(from, to)
}

case class Row(row: Int) extends AdjMatrixZone {
	def cells(nodeCount: Int, excludeSelfEdges: Boolean = false): Seq[Cell] = {
		(0 until nodeCount)
			.map(col => Cell(row, col))
			.filter(c => !(excludeSelfEdges && c.row == c.col))
	}
}

case class Column(column: Int) extends AdjMatrixZone {
	def cells(nodeCount: Int, excludeSelfEdges: Boolean = false): Seq[Cell] = {
		(0 until nodeCount)
			.map(row => Cell(row, column))
			.filter(c => !(excludeSelfEdges && c.row == c.col))
	}
}

case object Corner extends AdjMatrixZone // corner areas that won't highlight/select any cells

/** Cell area but when there are zero nodes, and therefore no 'cell' at all */
case object NoCell extends AdjMatrixZone