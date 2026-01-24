package graphcontroller.dataobject

import graphcontroller.dataobject.Vector2D

case class Rectangle(
	topLeft: Vector2D,
	width: Int,
	height: Int
) extends Shape {
	type This = Rectangle

	def bottomRight: Vector2D = Vector2D(topLeft.x + width, topLeft.y + height)

	def translate(vec: Vector2D): Rectangle =
		Rectangle(topLeft.translate(vec), width, height)

	def scaled(scaleFactor: Int): Rectangle = Rectangle(topLeft.scaled(scaleFactor), width * scaleFactor, height * scaleFactor)

	/** Note: this doesn't actually rotate the sides of the rectangle, just where it's topLeft point is located.
	 * The reason is that this Rectangle shape eventually correlates to the Canvas API ctx.rect(...) function, which
	 * will always draw a horizontal/vertical edged rectangle */
	def rotate(radians: Double): Rectangle = {
		// Rotating a rectangle around its top-left corner
		val newTopLeft = topLeft.rotate(radians)
		Rectangle(newTopLeft, width, height)
	}
}
