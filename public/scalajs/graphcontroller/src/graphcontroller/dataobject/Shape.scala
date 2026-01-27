package graphcontroller.dataobject

trait Shape {
	type This <: Shape

	def translate(vec2: Vector2D): This

	def scale(scaleFactor: Int): This

	/** Rotate around origin */
	def rotate(radians: Double): This
}
