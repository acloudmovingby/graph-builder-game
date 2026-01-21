package graphcontroller.dataobject

/**
 * Represents either a Cell or some area around the matrix
 */
case class AdjMatrixDimensions(
	canvasWidth: Int,
	canvasHeight: Int,
	padding: Int
) {
	def matrixWidth: Int = canvasWidth - (padding * 2)
	def matrixHeight: Int = canvasHeight - (padding * 2)
	def cellWidth(nodeCount: Int): Double = matrixWidth.toDouble / nodeCount.toDouble
	def cellHeight(nodeCount: Int): Double = matrixHeight.toDouble / nodeCount.toDouble
}