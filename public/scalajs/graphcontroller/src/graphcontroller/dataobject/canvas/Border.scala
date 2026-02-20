package graphcontroller.dataobject.canvas

/** Information used for border/line styling */
case class Border(
	color: String, // Hex string, e.g. "#FF0000" // TODO this correlates to ctx.strokeStyle ... can the "style" be something other than a color?
	width: Double,
	lineDashSegments: Seq[Int] = Seq.empty
)
