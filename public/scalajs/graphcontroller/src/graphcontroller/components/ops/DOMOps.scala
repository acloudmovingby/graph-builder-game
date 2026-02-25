package graphcontroller.components.ops

import graphcontroller.components.RenderOp
import org.scalajs.dom

/**
 * A generic RenderOp for setting an attribute on a DOM element.
 *
 * @param elementId The ID of the DOM element.
 * @param attribute The name of the attribute to set.
 * @param value     The value to set for the attribute.
 */
case class SetAttribute(elementId: String, attribute: String, value: String) extends RenderOp {
	override def render(): Unit = {
		val elem = dom.document.getElementById(elementId)
		if (elem != null) {
			elem.setAttribute(attribute, value)
		}
	}
}

/**
 * A RenderOp for removing an attribute from a DOM element.
 */
case class RemoveAttribute(elementId: String, attribute: String) extends RenderOp {
	override def render(): Unit = {
		val elem = dom.document.getElementById(elementId)
		if (elem != null) {
			elem.removeAttribute(attribute)
		}
	}
}

/**
 * A RenderOp that does nothing.
 */
case object NoOp extends RenderOp {
	override def render(): Unit = ()
}

/**
 * A RenderOp for setting a style property on a DOM element.
 */
case class SetStyleProperty(elementId: String, property: String, value: String) extends RenderOp {
	override def render(): Unit = {
		val elem = dom.document.getElementById(elementId).asInstanceOf[dom.html.Element]
		if (elem != null) {
			elem.style.setProperty(property, value)
		}
	}
}

/**
 * A RenderOp for this kind of code:
 * document.getElementById("node-count").innerHTML = nodeCount;
*/
case class SetInnerHTML(elementId: String, html: String) extends RenderOp {
	override def render(): Unit = {
		val elem = dom.document.getElementById(elementId)
		if (elem != null) {
			elem.innerHTML = html
		}
	}
}