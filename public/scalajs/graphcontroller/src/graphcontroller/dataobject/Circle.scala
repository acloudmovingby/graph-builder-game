package graphcontroller.dataobject

import graphcontroller.dataobject.{Vector2D, PointJS}

import scala.scalajs.js

case class Circle(
	center: Vector2D,
	radius: Int
) extends Shape {
	type This = Circle

	def translate(vec: Vector2D): Circle = this.copy(center = center.translate(vec))

	def scale(scaleFactor: Int): Circle = this.copy(radius = radius * scaleFactor)

	// takes radians but produces a Circle with Int points. Radians are small...
	def rotate(radians: Double): Circle = this.copy(center = center.rotate(radians))
}

