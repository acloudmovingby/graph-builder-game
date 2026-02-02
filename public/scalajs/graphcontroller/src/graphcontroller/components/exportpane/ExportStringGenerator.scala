package graphcontroller.components.exportpane

import graphi.MapGraph
import graphcontroller.shared.AdjacencyExportType
import ExportFormat.*
import graphcontroller.shared.AdjacencyExportType.Matrix


object ExportStringGenerator {
	def generate(
		graph: MapGraph[Int, ?],
		format: ExportFormat,
		adjType: AdjacencyExportType
	): String = {
		format match {
			case DOT => graph.toDot
			case Python =>
				adjType match {
					case AdjacencyExportType.List =>
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"  $node: ${neighbors.mkString("[", ", ", "]")}"
						}.toSeq
						if (entries.nonEmpty) entries.mkString("{\n", ",\n", "\n}") else "{}"
					case AdjacencyExportType.Matrix =>
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
						// Output as Python 2D list
						if (matrix.nonEmpty) {
							val rows = matrix.map(row => row.mkString("[", ", ", "]")).mkString(",\n  ")
							s"[\n  $rows\n]"
						} else "[]"
				}
			case Java =>
				adjType match {
					case AdjacencyExportType.List =>
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"    put($node, Arrays.asList(${neighbors.mkString(", ")}));"
						}.mkString("\n")
						if (entries.nonEmpty) s"new HashMap<Integer, List<Integer>>() {{\n$entries\n}};"
						else s"new HashMap<Integer, List<Integer>>() {{}};"
					case AdjacencyExportType.Matrix =>
						val nodes = graph.adjMap.keys.toList.sorted
						val nodeIdx = nodes.zipWithIndex.toMap
						val size = nodes.size
						val matrix = Array.fill(size, size)(0)
						for ((from, neighbors) <- graph.adjMap; to <- neighbors) {
							val i = nodeIdx(from)
							val j = nodeIdx(to)
							matrix(i)(j) = 1
						}
						val rows = matrix.map(row => row.mkString("{", ", ", "}")).mkString(",\n  ")
						if (rows.nonEmpty) s"new int[][] {\n  $rows\n};"
						else "new int[][] {};"
				}
			case JSON =>
				adjType match {
					case AdjacencyExportType.List =>
						// very similar to the Python implementation, but we have to quote keys. To make it consistent, also
						// quoting the nodes in the values, though later we can make that a user-selectable option
						val entries = graph.adjMap.map { case (node, neighbors) =>
							s"""  "$node": ${neighbors.map(n => s"\"$n\"").mkString("[", ", ", "]")}"""
						}.toSeq
						if (entries.nonEmpty) entries.mkString("{\n", ",\n", "\n}") else "{}"
					case AdjacencyExportType.Matrix =>
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
						// Output as Python 2D list
						if (matrix.nonEmpty) {
							val rows = matrix.map(row => row.mkString("[", ", ", "]")).mkString(",\n  ")
							s"[\n  $rows\n]"
						} else "[]"
				}
			case _ => "Unimplemented"
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
}
