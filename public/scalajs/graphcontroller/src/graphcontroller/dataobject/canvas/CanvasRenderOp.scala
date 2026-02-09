package graphcontroller.dataobject.canvas

import org.scalajs.dom.CanvasRenderingContext2D

import graphcontroller.dataobject.Shape

trait CanvasRenderOp extends Shape {
	type This <: CanvasRenderOp
	def draw(ctx: CanvasRenderingContext2D): Unit
}