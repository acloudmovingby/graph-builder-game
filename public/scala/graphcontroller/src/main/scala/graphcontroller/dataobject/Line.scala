package graphcontroller.dataobject

import graphcontroller.dataobject.Vector2D

case class Line(from: Vector2D, to: Vector2D) extends Shape {
	type This = Line

	def translate(vec: Vector2D): Line =
		Line(from.translate(vec), to.translate(vec))

	def scaled(scaleFactor: Int): Line =
		Line(from.scaled(scaleFactor), to.scaled(scaleFactor))

	def rotate(radians: Double): Line =
		Line(from.rotate(radians), to.rotate(radians))
}
