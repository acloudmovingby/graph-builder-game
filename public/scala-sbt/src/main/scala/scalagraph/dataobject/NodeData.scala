package scalagraph.dataobject

import scala.scalajs.js

case class NodeData(counter: Int, x: Int, y: Int)

// This is a facade type for the JavaScript representation of NodeData, I think it has to be just raw values
// without methods, so I put the conversion methods in the singleton NodeDataConverter
@js.native
trait NodeDataJS extends js.Object {
	val counter: Int
	val x: Int
	val y: Int
}

object NodeDataConverter {
	def toJS(data: NodeData): NodeDataJS = {
		js.Dynamic.literal(
			counter = data.counter,
			x = data.x,
			y = data.y
		).asInstanceOf[NodeDataJS]
	}

	def toScala(js: NodeDataJS): NodeData = NodeData(js.counter, js.x, js.y)
}

trait KeyWithData extends js.Object {
	val key: Int
	val data: NodeDataJS
}

object KeyWithDataConverter {
	def toJS(_key: Int, _data: NodeData): KeyWithData = {
		val nodeDataJS = NodeDataConverter.toJS(_data)
		js.Dynamic.literal(
			key = _key,
			data = nodeDataJS
		).asInstanceOf[KeyWithData]
	}
}
