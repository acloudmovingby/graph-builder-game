package graphcontroller.dataobject

/**
 * Represents either a Cell or some area around the matrix
 */
case class AdjMatrixDimensions(
	canvasWidth: Int,
	canvasHeight: Int,
	padding: Int,
	numberPadding: Int = 10 // padding between the matrix and the row/column numbers
) {
	def matrixWidth: Int = canvasWidth - (padding * 2)
	def matrixHeight: Int = canvasHeight - (padding * 2)
}