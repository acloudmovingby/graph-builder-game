package graphcontroller.dataobject

import graphcontroller.dataobject.Vector2D

case class Line(from: Vector2D, to: Vector2D) extends Shape {
	type This = Line

	def translate(vec: Vector2D): Line =
		Line(from.translate(vec), to.translate(vec))

	def scale(scaleFactor: Int): Line =
		Line(from.scale(scaleFactor), to.scale(scaleFactor))

	def rotate(radians: Double): Line =
		Line(from.rotate(radians), to.rotate(radians))
}
