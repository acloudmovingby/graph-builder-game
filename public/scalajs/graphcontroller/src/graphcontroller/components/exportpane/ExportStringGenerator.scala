package graphcontroller.components.exportpane

import graphi.MapGraph
import graphcontroller.shared.GraphRepresentation
import ExportFormat.*
import graphcontroller.shared.GraphRepresentation.Matrix

import scala.collection.immutable.ArraySeq


object ExportStringGenerator {
	val INDENT = "    " // TODO make this an option to the user (2 space, 4 spaces, tab, etc.)

	/** 
	 * Generates the export string for the given graph, format, and representation type.
	 * 
	 * There's definitely some repetition in here in places but I'm feeling lazy and also I think it could make it brittle
	 * to try and abstract away some of the commonality. 
	 */
	def generate(
		graph: MapGraph[Int, ?],
		format: ExportFormat,
		adjType: GraphRepresentation
	): String = {
		format match {
			case DOT => graph.toDot
			case Python =>
				adjType match {
					case GraphRepresentation.List =>
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"$INDENT$node: ${neighbors.mkString("[", ", ", "]")}"
						}.toSeq
						if (entries.nonEmpty) entries.mkString("{\n", ",\n", "\n}") else "{}"
					case GraphRepresentation.Matrix =>
						val matrix = generateMatrix(graph)
						// Output as Python 2D list
						if (matrix.nonEmpty) {
							val rows = matrix.map(row => row.mkString("[", ", ", "]")).mkString(s",\n$INDENT")
							s"[\n$INDENT$rows\n]"
						} else "[]"
				}
			case Java =>
				adjType match {
					case GraphRepresentation.List =>
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"${INDENT}put($node, Arrays.asList(${neighbors.mkString(", ")}));"
						}.mkString("\n")
						if (entries.nonEmpty) s"new HashMap<Integer, List<Integer>>() {{\n$entries\n}};"
						else s"new HashMap<Integer, List<Integer>>() {{}};"
					case GraphRepresentation.Matrix =>
						val matrix = generateMatrix(graph)
						val rows = matrix.map(row => row.mkString("{", ", ", "}")).mkString(s",\n$INDENT")
						if (rows.nonEmpty) s"new int[][] {\n$INDENT$rows\n};"
						else "new int[][] {};"
				}
			case JSON =>
				adjType match {
					case GraphRepresentation.List =>
						// very similar to the Python implementation, but we have to quote keys. To make it consistent, also
						// quoting the nodes in the values, though later we can make that a user-selectable option
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"""$INDENT"$node": ${neighbors.map(n => s"\"$n\"").mkString("[", ", ", "]")}"""
						}.toSeq
						if (entries.nonEmpty) entries.mkString("{\n", ",\n", "\n}") else "{}"
					case GraphRepresentation.Matrix =>
						val matrix = generateMatrix(graph)
						// Output as Python 2D list
						if (matrix.nonEmpty) {
							val rows = matrix.map(row => row.mkString("[", ", ", "]")).mkString(s",\n$INDENT")
							s"[\n$INDENT$rows\n]"
						} else "[]"
				}
			case Scala =>
				adjType match {
					case GraphRepresentation.List => graph.adjMap.toString()
					case GraphRepresentation.Matrix =>
						val matrix = generateMatrix(graph)
						// Output as Python 2D list
						if (matrix.nonEmpty) {
							val rows = matrix.map(row => row.mkString("Vector(", ", ", ")")).mkString(s",\n$INDENT")
							s"Vector(\n$INDENT$rows\n)"
						} else "Vector()"
				}
		}
	}

	def escapeHtml(input: String): String = {
		input
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&apos;")
	}

	/** AI generated this. Why are we using Array? */
	private def generateMatrix(graph: MapGraph[Int, ?]): Array[Array[Int]] = {
		// Get sorted node list for consistent matrix
		val nodes = graph.adjMap.keys.toList.sorted
		val nodeIdx = nodes.zipWithIndex.toMap
		val size = nodes.size
		val matrix = Array.fill(size, size)(0)
		for ((from, neighbors) <- graph.adjMap; to <- neighbors) {
			val i = nodeIdx(from)
			val j = nodeIdx(to)
			matrix(i)(j) = 1
		}
		matrix
	}
}
