package scalagraph.dataobject

import scala.scalajs.js

case class NodeData(counter: Int, x: Int, y: Int) {
	def toJS: NodeDataJS = {
		js.Dynamic.literal(
			counter = this.counter,
			x = this.x,
			y = this.y
		).asInstanceOf[NodeDataJS]
	}
}

object NodeData {
	def fromJS(js: NodeDataJS): NodeData = NodeData(js.counter, js.x, js.y)
}

// This is a facade type for the JavaScript representation of NodeData
@js.native
trait NodeDataJS extends js.Object {
	val counter: Int
	val x: Int
	val y: Int
}

trait KeyWithData extends js.Object {
	val key: Int
	val data: NodeDataJS
}

object KeyWithDataConverter {
	def toJS(_key: Int, _data: NodeData): KeyWithData = {
		js.Dynamic.literal(
			key = _key,
			data = _data.toJS
		).asInstanceOf[KeyWithData]
	}
}
