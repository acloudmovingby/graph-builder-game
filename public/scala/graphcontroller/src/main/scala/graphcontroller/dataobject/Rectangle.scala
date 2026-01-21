package graphcontroller.dataobject

import graphcontroller.dataobject.Point

case class Rectangle(
	topLeft: Point,
	width: Int,
	height: Int
) {
	def bottomRight: Point = Point(topLeft.x + width, topLeft.y + height)

	def translate(vec: Point): Rectangle =
		Rectangle(topLeft.translate(vec), width, height)

	def scaled(scaleFactor: Int): Rectangle =
		Rectangle(topLeft.scaled(scaleFactor), width * scaleFactor, height * scaleFactor)
}
