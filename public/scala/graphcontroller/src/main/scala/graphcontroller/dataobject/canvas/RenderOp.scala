package graphcontroller.dataobject.canvas

import org.scalajs.dom.CanvasRenderingContext2D

import graphcontroller.dataobject.Shape

trait RenderOp extends Shape {
	def draw(ctx: CanvasRenderingContext2D): Unit
}

